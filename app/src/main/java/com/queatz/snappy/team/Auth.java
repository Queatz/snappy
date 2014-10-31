package com.queatz.snappy.team;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.queatz.snappy.Config;
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
    private static final String KEY_AUTHENTICATED = "is_authenticated";
    protected static final int REQUEST_CODE_RESOLUTION = 1;

    public Team team;

    private GoogleApiClient mGoogleApiClient;
    private boolean mIsInResolution;
    private boolean mIsAuthenticated;

    public Auth(Team t) {
        team = t;

        mGoogleApiClient = new GoogleApiClient.Builder(team.context)
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    public void save() {
        Log.d(Config.TAG, "[Auth Save] " + mIsAuthenticated);
        team.preferences.edit().putBoolean(KEY_AUTHENTICATED, mIsAuthenticated).apply();
    }

    public void load() {
        Log.d(Config.TAG, "[Auth Load] " + mIsAuthenticated);
        mIsAuthenticated = team.preferences.getBoolean(KEY_AUTHENTICATED, false);
    }

    public boolean isAuthenticated() {
        return mIsAuthenticated;
    }

    public void showMain() {
        team.view.pop(new ViewActivity.OnCompleteCallback() {
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
        if(mIsAuthenticated) {
            showMain();
            return;
        }

        mGoogleApiClient.connect();
    }

    public void fromBundle(Bundle bundle) {
        if (bundle != null) {
            mIsInResolution = bundle.getBoolean(KEY_IN_RESOLUTION, false);
        }

        load();
    }

    public void toBundle(Bundle bundle) {
        bundle.putBoolean(KEY_IN_RESOLUTION, mIsInResolution);

        save();
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
            mIsAuthenticated = false;
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "GoogleApiClient connected");

        mIsAuthenticated = true;

        showMain();
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "GoogleApiClient connection suspended");
        retryConnecting();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        mIsAuthenticated = false;
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
