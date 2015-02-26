package com.queatz.snappy.team;

import android.content.Context;
import android.location.Criteria;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.queatz.snappy.Config;
import com.queatz.snappy.R;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by jacob on 10/19/14.
 */
public class Location implements LocationListener {
    public Team team;
    private android.location.Location mLocation;
    private LocationManager mLocationManager;

    public static interface AutocompleteCallback {
        public void onResult(JSONObject result);
    }

    public Location(Team t) {
        team = t;
        mLocationManager = (LocationManager) team.context.getSystemService(Context.LOCATION_SERVICE);
    }

    public android.location.Location get() {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        String provider = mLocationManager.getBestProvider(criteria, true);

        mLocation = mLocationManager.getLastKnownLocation(provider);

        if(!LocationManager.NETWORK_PROVIDER.equals(provider)) {
            android.location.Location networkLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if(mLocation == null || networkLocation.getAccuracy() < mLocation.getAccuracy())
                mLocation = networkLocation;
        }

        return mLocation;
    }

    public void locate() {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        String provider = mLocationManager.getBestProvider(criteria, true);

        Log.w(Config.LOG_TAG, provider);

        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);

        if(!LocationManager.NETWORK_PROVIDER.equals(provider))
            mLocationManager.requestLocationUpdates(provider, 0, 0, this);
    }

    public void stopLocating() {
        mLocationManager.removeUpdates(this);
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

        if(location.getAccuracy() < Config.locationAccuracy)
            stopLocating();

        Log.d(Config.LOG_TAG, "Locating (" + location.getAccuracy() + ") : " + location.getLatitude() + ", " + location.getLongitude());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
