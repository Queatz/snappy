package com.queatz.snappy;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;

import com.google.android.gms.plus.Plus;

public class MainActivity extends Activity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "MainActivity";

    private static final String KEY_IN_RESOLUTION = "is_in_resolution";
    protected static final int REQUEST_CODE_RESOLUTION = 1;

    private GoogleApiClient mGoogleApiClient;
    private boolean mIsInResolution;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mIsInResolution = savedInstanceState.getBoolean(KEY_IN_RESOLUTION, false);
        }

        showTempView(0);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Plus.API)
                    .addScope(Plus.SCOPE_PLUS_LOGIN)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }

        if(false) // Already registered
            mGoogleApiClient.connect();

        updateUI();
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }

        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_IN_RESOLUTION, mIsInResolution);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
        case REQUEST_CODE_RESOLUTION:
            mIsInResolution = false;

            Log.d("snappy", resultCode + " " + data);
            if(data != null)
                retryConnecting();

            break;
        }
    }

    // Functions

    public void showTempView(int v) {
        switch (v) {
            case 0:
                setContentView(R.layout.welcome);

                findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mGoogleApiClient.connect();
                    }
                });

                findViewById(R.id.textView).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        updateUI();
                    }
                });

                findViewById(R.id.imageView).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showTempView(1);
                    }
                });

                break;
            case 1:
                setContentView(R.layout.upto);

                findViewById(R.id.into).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showTempView(2);
                    }
                });

                findViewById(R.id.me).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showTempView(3);
                    }
                });

                break;
            case 2:
                setContentView(R.layout.into);

                findViewById(R.id.upto).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showTempView(1);
                    }
                });

                findViewById(R.id.me).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showTempView(3);
                    }
                });

                break;
            case 3:
                setContentView(R.layout.person_upto);

                findViewById(R.id.into).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showTempView(4);
                    }
                });

                findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showTempView(1);
                    }
                });

                break;
            case 4:
                setContentView(R.layout.person_into);

                findViewById(R.id.upto).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showTempView(3);
                    }
                });

                findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showTempView(1);
                    }
                });

                break;
        }
    }

    // Google session stuff

    private void retryConnecting() {
        mIsInResolution = false;
        if (!mGoogleApiClient.isConnecting()) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "GoogleApiClient connected");

        updateUI();
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
                    result.getErrorCode(), this, 0, new OnCancelListener() {
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
            result.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);
        } catch (SendIntentException e) {
            Log.e(TAG, "Exception while starting resolution activity", e);
            retryConnecting();
        }
    }

    public void updateUI() {
        if(mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            if(Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
                Log.d("SNAPPY", "name = " + Plus.PeopleApi.getCurrentPerson(mGoogleApiClient).getDisplayName());

                ((TextView) findViewById(R.id.textView))
                        .setText(Plus.PeopleApi.getCurrentPerson(mGoogleApiClient).getDisplayName());
            }
            else {
                Log.d("SNAPPY", "It's NULL");
            }
        }
    }
}
