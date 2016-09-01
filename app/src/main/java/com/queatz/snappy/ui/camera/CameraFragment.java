package com.queatz.snappy.ui.camera;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorEventListener2;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.queatz.snappy.MainApplication;
import com.queatz.snappy.R;
import com.queatz.snappy.Util;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.team.Team;
import com.squareup.picasso.Picasso;

import java.io.BufferedInputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by jacob on 8/27/16.
 */

public class CameraFragment extends Fragment {

    Team team;

    /**
     * Thread Management
     */

    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;

    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        if (mBackgroundThread == null) {
            return;
        }

        mBackgroundThread.quitSafely();

        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        team = ((MainApplication) getActivity().getApplication()).team;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            mCameraManager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
        }

        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
    }

    @Override
    public void onResume() {
        super.onResume();
        startBackgroundThread();
        resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopBackgroundThread();
        pause();
    }

    /**
     * View Management
     */

    private Surface mSurface;
    private View mCameraViewContainer;
    private TextureView mTextureView;
    private Size mPreviewSize;
    private View mActionsView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.camera, container, false);

        // XXX TODO camera not supported pre-lollipop
        if (mCameraManager == null) {
            return view;
        }

        mActionsView = view.findViewById(R.id.actions);
        mCameraViewContainer = view;

        mTextureView = ((TextureView) view.findViewById(R.id.cameraSurface));

        // Tapping the preview will either capture an image or resume the preview
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mActionsView.getVisibility() == View.VISIBLE) {
                    resume();
                } else {
                    capture();
                }
            }
        });

        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                switchCamera();
                return true;
            }
        });

        return view;
    }

    private void resume() {
        if (getView() != null) {
            getView().post(new Runnable() {
                @Override
                public void run() {
                    team.view.keyboard(getActivity().getWindow().getDecorView().getWindowToken());
                }
            });
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        }

        if (mActionsView.getVisibility() == View.VISIBLE) {
            mActionsView.post(new Runnable() {
                @Override
                public void run() {
                    mActionsView.setVisibility(View.GONE);
                }
            });
        }

        if (mDeviceOrientation == null) {
            mDeviceOrientation = new DeviceOrientation();
            mSensorManager.registerListener(mDeviceOrientation.getEventListener(),
                    mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
                    SensorManager.SENSOR_DELAY_UI);
        }

        if (mCamera != null) try {
            if (mSurface != null) {
                CaptureRequest.Builder builder = mCamera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                builder.addTarget(mSurface);
                builder.set(CaptureRequest.CONTROL_AF_MODE,
                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                mSession.stopRepeating();
                mSession.setRepeatingRequest(builder.build(),
                        new CameraCaptureSession.CaptureCallback() {
                        }, mBackgroundHandler);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } else {
            setCamera(CameraCharacteristics.LENS_FACING_BACK);
        }
    }

    private void pause() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }

        endLastCamera();
    }

    /**
     * Camera Management
     */

    private CameraDevice mCamera;
    private CameraCharacteristics mCharacteristics;
    private CameraCaptureSession mSession;
    private StreamConfigurationMap mStreamConfigurationMap;
    private ImageReader mImageReader;
    private CameraManager mCameraManager;
    private SensorManager mSensorManager;
    private DeviceOrientation mDeviceOrientation;

    private void initCamera() {
        if (mCamera == null) {
            return;
        }

        configureTransform(mCameraViewContainer.getMeasuredWidth(), mCameraViewContainer.getMeasuredHeight());

        mTextureView.getSurfaceTexture()
                .setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());

        if (mSurface != null) {
            mSurface.release();
        }

        mSurface = new Surface(mTextureView.getSurfaceTexture());

        if (mImageReader != null) {
            mImageReader.close();
        }

        mImageReader = ImageReader.newInstance(mPreviewSize.getWidth(), mPreviewSize.getHeight(),
                ImageFormat.JPEG, 2);

        mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(final ImageReader reader) {
//                final ImageView examplePhoto = (ImageView) getView().findViewById(R.id.examplePhoto);
//                examplePhoto.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        Picasso.with(getActivity()).load(Util.uriFromImage(reader.acquireLatestImage()))
//                                .into(examplePhoto);
//                    }
//                });
            }
        }, mBackgroundHandler);

        List<Surface> surfaces = Arrays.asList(mSurface, mImageReader.getSurface());

        try {
            mCamera.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    mSession = session;
                    resume();
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                }
            }, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void capture() {
        if (mCamera == null) {
            return;
        }

        try {
            CaptureRequest.Builder builder = mCamera.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            builder.addTarget(mImageReader.getSurface());

            // Orientation
            final int deviceRotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
            final int orientation = mDeviceOrientation.getOrientation();
            final int rotation = getCombinedRotation(deviceRotation, orientation);

            int jpeg = getOrientation((getRotation(deviceRotation) - rotation + 360) % 360);

            builder.set(CaptureRequest.JPEG_ORIENTATION, jpeg);

            mSession.stopRepeating();
            mSession.capture(builder.build(), new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    showCaptureActions(rotation);
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private int getCombinedRotation(int deviceRotation, int orientation) {
        return orientation + getRotation(deviceRotation);
    }

    private int getRotation(int orientation) {
        switch (orientation) {
            case Surface.ROTATION_0:
                return 0;
            case Surface.ROTATION_90:
                return 90;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_270:
                return 270;
            default:
                return 0;
        }
    }

    private void showCaptureActions(int orientation) {
        mActionsView.setVisibility(View.VISIBLE);

        ImageView usePhoto = (ImageView) mActionsView.findViewById(R.id.usePhoto);

        usePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                team.camera.supplyPhoto(mImageReader.acquireLatestImage());
            }
        });

        usePhoto.setRotation(-orientation);
    }

    private void switchCamera() {
        if (mCharacteristics != null && mCharacteristics.get(CameraCharacteristics.LENS_FACING).equals(CameraCharacteristics.LENS_FACING_BACK)) {
            setCamera(CameraCharacteristics.LENS_FACING_FRONT);
        } else {
            setCamera(CameraCharacteristics.LENS_FACING_BACK);
        }
    }

    private void setCamera(final Integer facing) {
        if (!team.camera.isPermissionGranted()) {
            team.camera.whenAvailable(new Runnable() {
                @Override
                public void run() {
                    setCamera(facing);
                }
            });
            return;
        }

        try {
            String cam = null;

            String[] cameras = mCameraManager.getCameraIdList();

            for (String camera : cameras) {
                cam = camera;
                mCharacteristics = mCameraManager.getCameraCharacteristics(camera);

                if (facing.equals(mCharacteristics.get(CameraCharacteristics.LENS_FACING))) {
                    mStreamConfigurationMap = mCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                    break;
                }
            }

            if (cam == null) {
                return;
            }

            mSensorOrientation = mCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);

            if (mCamera != null) {
                endLastCamera();
            }

            mCameraManager.openCamera(cam, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    mCamera = camera;
                    setupTexture();
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {
                    CameraFragment.this.onDestroy();
                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {

                }
            }, mBackgroundHandler);
        } catch (SecurityException | CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void endLastCamera() {
        if (mCamera != null) {
            mCamera.close();
            mCamera = null;
        }

        if (mSession != null) {
            mSession.close();
            mSession = null;
        }

        if (mImageReader != null) {
            mImageReader.close();
            mImageReader = null;
        }

        if (mSurface != null) {
            mSurface.release();
            mSurface = null;
        }

        if (mSession != null) {
            mSession.close();
            mSession = null;
        }

        if (mDeviceOrientation != null) {
            mSensorManager.unregisterListener(mDeviceOrientation.getEventListener());
            mDeviceOrientation = null;
        }
    }

    private void setupTexture() {
        final boolean isInit;

        if (mTextureView.isAvailable()) {
            mTextureView.post(new Runnable() {
                @Override
                public void run() {
                    initCamera();
                }
            });

            isInit = true;
        } else {
            isInit = false;
        }

        mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                if (!isInit && mSurface == null) {
                    initCamera();
                }
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                configureTransform(width, height);
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                if (mSurface != null) {
                    mSurface.release();
                    mSurface = null;
                }

                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });
    }

    /**
     * Utilities
     */

    private int mSensorOrientation;

    private static Size chooseOptimalSize(Size[] choices,
                                          int textureViewWidth,
                                          int textureViewHeight,
                                          int maxWidth,
                                          int maxHeight,
                                          Size aspectRatio) {

        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Size> bigEnough = new ArrayList<>();
        // Collect the supported resolutions that are smaller than the preview Surface
        List<Size> notBigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getWidth() <= maxWidth && option.getHeight() <= maxHeight &&
                    option.getHeight() == option.getWidth() * h / w) {
                if (option.getWidth() >= textureViewWidth && option.getHeight() >= textureViewHeight) {
                    bigEnough.add(option);
                } else {
                    notBigEnough.add(option);
                }
            }
        }

        // Pick the smallest of those big enough. If there is no one big enough, pick the
        // largest of those not big enough.
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else if (notBigEnough.size() > 0) {
            return Collections.max(notBigEnough, new CompareSizesByArea());
        } else {
            Log.e(Config.LOG_TAG, "Couldn't find any suitable preview size");
            return choices[0];
        }
    }


    private void configureTransform(int viewWidth, int viewHeight) {
        if (getView() == null) {
            return;
        }

        int displayRotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
        //noinspection ConstantConditions
        boolean swappedDimensions = false;
        switch (displayRotation) {
            case Surface.ROTATION_0:
            case Surface.ROTATION_180:
                if (mSensorOrientation == 90 || mSensorOrientation == 270) {
                    swappedDimensions = true;
                }
                break;
            case Surface.ROTATION_90:
            case Surface.ROTATION_270:
                if (mSensorOrientation == 0 || mSensorOrientation == 180) {
                    swappedDimensions = true;
                }
                break;
            default:
                Log.e(Config.LOG_TAG, "Display rotation is invalid: " + displayRotation);
        }

        Point displaySize = new Point();
        getActivity().getWindowManager().getDefaultDisplay().getSize(displaySize);
        int rotatedPreviewWidth = viewWidth;
        int rotatedPreviewHeight = viewHeight;
        int maxPreviewWidth = displaySize.x;
        int maxPreviewHeight = displaySize.y;

        if (swappedDimensions) {
            rotatedPreviewWidth = viewHeight;
            rotatedPreviewHeight = viewWidth;
            maxPreviewWidth = displaySize.y;
            maxPreviewHeight = displaySize.x;
        }

        Size sizes[] = mStreamConfigurationMap.getOutputSizes(SurfaceTexture.class);
        mPreviewSize = chooseOptimalSize(sizes,
                rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth,
                maxPreviewHeight, new Size(viewWidth, viewHeight));

        int orientation = getResources().getConfiguration().orientation;
        float aspect = (float) mPreviewSize.getWidth() / (float) mPreviewSize.getHeight();

        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            aspect = 1f / aspect;
        }

        ((AspectView) getView().findViewById(R.id.cameraBoxing)).setAspect(aspect);

        Activity activity = getActivity();

        if (null == mTextureView || null == mPreviewSize || null == activity) {
            return;
        }

        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();

        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / mPreviewSize.getHeight(),
                    (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }
        mTextureView.setTransform(matrix);
    }

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(0, 90);
        ORIENTATIONS.append(90, 0);
        ORIENTATIONS.append(180, 270);
        ORIENTATIONS.append(270, 180);
    }

    /**
     * Retrieves the JPEG orientation from the specified screen rotation.
     *
     * @param rotation The screen rotation.
     * @return The JPEG orientation (one of 0, 90, 270, and 360)
     */
    private int getOrientation(int rotation) {
        // Sensor orientation is 90 for most devices, or 270 for some devices (eg. Nexus 5X)
        // We have to take that into account and rotate JPEG properly.
        // For devices with orientation of 90, we simply return our mapping from ORIENTATIONS.
        // For devices with orientation of 270, we need to rotate the JPEG 180 degrees.
        return (ORIENTATIONS.get(rotation) + mSensorOrientation + 270) % 360;
    }

    static class CompareSizesByArea implements Comparator<Size> {
        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }

    }

}
