package com.queatz.snappy.team;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Criteria;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.queatz.snappy.Config;
import com.queatz.snappy.MainActivity;
import com.queatz.snappy.R;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by jacob on 10/19/14.
 */
public class Location implements
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        ResultCallback<LocationSettingsResult> {
    public Team team;
    private android.location.Location mLocation;
    private final ArrayList<OnLocationFoundCallback> mCallbacks = new ArrayList<>();
    private GoogleApiClient mGoogleApiClient;
    private Activity mActivity;
    private LocationRequest mLocationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(3000)
            .setFastestInterval(1000);
    private boolean mLocationIsAvailable = true;
    private ArrayList<Runnable> mRunWhenConnected = new ArrayList<>();

    public interface OnLocationFoundCallback {
        void onLocationFound(android.location.Location location);
        void onLocationUnavailable();
    }

    public interface AutocompleteCallback {
        void onResult(JSONObject result);
    }

    @Override
    public void onConnected(Bundle bundle) {
        synchronized (mRunWhenConnected) {
            while (!mRunWhenConnected.isEmpty()) {
                mRunWhenConnected.remove(0).run();
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    public Location(Team t) {
        team = t;
        mGoogleApiClient = new GoogleApiClient.Builder(team.context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mGoogleApiClient.connect();
    }

    private void ensureConnected(Runnable runnable) {
        if(mGoogleApiClient.isConnected())
            runnable.run();
        else
            mRunWhenConnected.add(runnable);
    }

    public void get(Activity activity, @NonNull OnLocationFoundCallback callback) {
        android.location.Location location = get();

        if(location != null) {
            callback.onLocationFound(location);
        }
        else {
            mCallbacks.add(callback);
            locate(activity);
        }
    }

    public android.location.Location get() {
        if(mGoogleApiClient.isConnected())
            mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        return mLocation;
    }

    public void locate(Activity activity) {
        mActivity = activity;

        ensureConnected(new Runnable() {
            @Override
            public void run() {
                turnOnLocationServices();

                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, Location.this);
            }
        });
    }

    private void locationNotAvailable() {
        synchronized (mCallbacks) {
            for(OnLocationFoundCallback callback : mCallbacks)
                callback.onLocationUnavailable();
        }
    }

    public boolean enabled() {
        return mLocationIsAvailable;
    }

    private void turnOnLocationServices() {
        LocationSettingsRequest locationSettingsRequest = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest).setAlwaysShow(true).build();

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(
                        mGoogleApiClient,
                        locationSettingsRequest
                );

        result.setResultCallback(Location.this);
    }

    @Override
    public void onResult(LocationSettingsResult locationSettingsResult) {
        switch (locationSettingsResult.getStatus().getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                mLocationIsAvailable = true;
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                mLocationIsAvailable = false;
                locationNotAvailable();

                try {
                    locationSettingsResult.getStatus().startResolutionForResult(mActivity, Config.REQUEST_CODE_CHECK_SETTINGS);
                }
                catch (IntentSender.SendIntentException e) {
                }
            default:
                mLocationIsAvailable = false;
                break;
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Config.REQUEST_CODE_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        mLocationIsAvailable = true;
                        break;
                    default:
                        break;
                }

                break;
        }
    }

    public void stopLocating() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    public void getTopGoogleLocationForInput(@NonNull final String input, @NonNull final AutocompleteCallback callback) {
        android.location.Location location = get();

        if(location == null)
            return;

        String url = String.format(Config.GOOGLE_PLACES_AUTOCOMPLETE_URL,
                location.getLatitude(),
                location.getLongitude(),
                input,
                team.context.getString(R.string.google_api_key));

        team.api.getInternalClient().get(url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                if(responseBody == null)
                    return;

                try {
                    JSONObject o = new JSONObject(new String(responseBody));

                    String url = String.format(Config.GOOGLE_PLACES_DETAILS_URL,
                            o.getJSONArray("predictions").getJSONObject(0).getString("place_id"),
                            team.context.getString(R.string.google_api_key));

                    team.api.getInternalClient().get(url, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            if(responseBody == null)
                                return;

                            try {
                                JSONObject o = new JSONObject(new String(responseBody));
                                callback.onResult(o.getJSONObject("result"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {}
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {}
        });
    }

    @Override
    public void onLocationChanged(android.location.Location location) {
        mLocation = location;

        if(location.getAccuracy() < Config.locationAccuracy) {
            synchronized (mCallbacks) {
                while (!mCallbacks.isEmpty())
                    mCallbacks.remove(0).onLocationFound(location);
            }

            stopLocating();
        }

        Log.d(Config.LOG_TAG, "Locating (" + location.getAccuracy() + ") : " + location.getLatitude() + ", " + location.getLongitude());
    }
}
