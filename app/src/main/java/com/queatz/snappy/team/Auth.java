package com.queatz.snappy.team;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.queatz.snappy.MainActivity;
import com.queatz.snappy.activity.ViewActivity;
import com.queatz.snappy.transition.Transition;

/**
 * Created by jacob on 10/19/14.
 */
public class Auth implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private String TAG = "snappy.team.auth";

    private static final String KEY_IN_RESOLUTION = "is_in_resolution";
    protected static final int REQUEST_CODE_RESOLUTION = 1;

    public Team team;

    private GoogleApiClient mGoogleApiClient;
    private boolean mIsInResolution;

    public Auth(Team t) {
        team = t;

        mGoogleApiClient = new GoogleApiClient.Builder(team.context)
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    public boolean isSignedIn() {
        return mGoogleApiClient.isConnected();
    }

    public void showMain() {
        team.view.pop(ViewActivity.Transition.GRAND_REVEAL, null, new ViewActivity.OnCompleteCallback() {
            @Override
            public void onComplete() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        team.view.push(ViewActivity.Transition.SPACE_GAME, null, ((MainActivity) team.view).mMainView);
                    }
                }, 1000);
            }
        });
    }

    public void signin() {
        if(mGoogleApiClient.isConnected()) {
            showMain();
            return;
        }

        mGoogleApiClient.connect();
    }

    public void fromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mIsInResolution = savedInstanceState.getBoolean(KEY_IN_RESOLUTION, false);
        }
    }

    public void toBundle(Bundle outState) {
        outState.putBoolean(KEY_IN_RESOLUTION, mIsInResolution);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_RESOLUTION:
                mIsInResolution = false;

                Log.d("snappy", resultCode + " " + data);
                if(data != null)
                    retryConnecting();

                break;
        }
    }

    private void retryConnecting() {
        mIsInResolution = false;
        if (!mGoogleApiClient.isConnecting()) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "GoogleApiClient connected");

        showMain();
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "GoogleApiClient connection suspended");
        retryConnecting();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "GoogleApiClient connection failed: " + result.toString());

        if (!result.hasResolution()) {
            GooglePlayServicesUtil.getErrorDialog(
                    result.getErrorCode(), team.view, 0, new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            retryConnecting();
                        }
                    }).show();
            return;
        }

        if (mIsInResolution) {
            return;
        }

        mIsInResolution = true;

        try {
            result.startResolutionForResult(team.view, REQUEST_CODE_RESOLUTION);
        } catch (IntentSender.SendIntentException e) {
            Log.e(TAG, "Exception while starting resolution activity", e);
            retryConnecting();
        }
    }
}
