package com.queatz.snappy.team;

import android.content.Context;
import android.location.Criteria;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.queatz.snappy.Config;

/**
 * Created by jacob on 10/19/14.
 */
public class Location implements LocationListener {
    public Team team;
    private android.location.Location mLocation;
    private LocationManager mLocationManager;

    public Location(Team t) {
        team = t;
        mLocationManager = (LocationManager) team.context.getSystemService(Context.LOCATION_SERVICE);
    }

    public android.location.Location get() {
        if(mLocation == null) {
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            String provider = mLocationManager.getBestProvider(criteria, true);


            mLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            if(mLocation == null || mLocation.getAccuracy() > Config.locationAccuracy && !LocationManager.NETWORK_PROVIDER.equals(provider)) {
                mLocation = mLocationManager.getLastKnownLocation(provider);
            }
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
