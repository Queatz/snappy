package com.queatz.snappy.team;

import android.Manifest;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;

import com.queatz.snappy.R;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.ui.camera.CameraFragment;

import java.util.ArrayList;

/**
 * Created by jacob on 8/28/16.
 */

public class Camera {
    Team team;
    private Callback mCallback;
    private CameraFragment mCameraFragment;
    private Activity mActivity;
    final private ArrayList<Runnable> mRunWhenConnected = new ArrayList<>();

    public interface Callback {
        void onPhoto(Uri uri);
        void onClosed();
    }

    Camera(Team team) {
        this.team = team;
    }

    public boolean isOpen() {
        return mActivity != null &&
                mActivity.findViewById(R.id.cameraLayout).getVisibility() == View.VISIBLE;
    }

    public void onPermissionGranted(String permission) {
        if (Manifest.permission.CAMERA.equals(permission)) {
            if (mActivity != null) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        cameraAvailable();
                    }
                });
            }
        }
    }

    private void cameraAvailable() {
        synchronized (mRunWhenConnected) {
            while (!mRunWhenConnected.isEmpty()) {
                mRunWhenConnected.remove(0).run();
            }
        }
    }

    public boolean isPermissionGranted() {
        return team.auth.checkPermission(mActivity, Manifest.permission.CAMERA);
    }

    public void whenAvailable(Runnable runnable) {
        mRunWhenConnected.add(runnable);
    }

    public void getPhoto(Activity activity, Callback callback) {
        View cameraLayout = activity.findViewById(R.id.cameraLayout);

        if (cameraLayout == null) {
            Log.w(Config.LOG_TAG, "No camera layout found for activity");
            return;
        }

        if (mCallback != null) {
            Log.w(Config.LOG_TAG, "Cannot have multiple camera requests at the same time");
        }

        mCallback = callback;
        mActivity = activity;

        if (mCameraFragment == null) {
            mCameraFragment = new CameraFragment();
        }

        // Show camera view
        cameraLayout.setVisibility(View.VISIBLE);
        FragmentTransaction transaction = activity.getFragmentManager().beginTransaction();
        transaction.add(R.id.cameraLayout, mCameraFragment, null);
        transaction.commitAllowingStateLoss();
    }

    public void close() {
        if (mActivity == null) {
            Log.w(Config.LOG_TAG, "Couldn't close camera because it wasn't opened");
            return;
        }

        FragmentTransaction transaction = mActivity.getFragmentManager().beginTransaction();
        transaction.remove(mCameraFragment);
        transaction.commitAllowingStateLoss();
        mActivity.findViewById(R.id.cameraLayout).setVisibility(View.GONE);
        mActivity = null;
    }

    public void supplyPhoto(Uri uri) {
        if (mCallback != null) {
            mCallback.onPhoto(uri);
        }

        close();
    }
}
