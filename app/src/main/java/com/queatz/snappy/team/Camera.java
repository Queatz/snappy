package com.queatz.snappy.team;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.media.Image;
import android.media.ImageReader;
import android.util.Log;
import android.view.View;

import com.queatz.snappy.R;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.ui.camera.CameraFragment;

/**
 * Created by jacob on 8/28/16.
 */

public class Camera {
    Team team;
    private Callback mCallback;
    private CameraFragment mCameraFragment;
    private Activity mActivity;

    public interface Callback {
        void onPhoto(Image image);
        void onClosed();
    }

    Camera(Team team) {
        this.team = team;
    }

    public boolean isOpen() {
        return mActivity != null &&
                mActivity.findViewById(R.id.cameraLayout).getVisibility() == View.VISIBLE;
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

    public void supplyPhoto(Image image) {
        if (mCallback != null) {
            mCallback.onPhoto(image);
        }

        close();
    }
}
