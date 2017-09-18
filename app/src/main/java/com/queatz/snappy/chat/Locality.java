package com.queatz.snappy.chat;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.queatz.snappy.team.Team;

import java.io.IOException;
import java.util.List;

/**
 * Created by jacob on 9/18/17.
 */

public class Locality {

    private final Team team;
    private final Geocoder geocoder;
    private String locality;

    public interface OnLocalityFound {
        void onLocalityFound(String locality);
    }

    public Locality(Team team) {
        this.team = team;
        geocoder = new Geocoder(team.context);
    }

    public String get() {
        return locality;
    }

    public void get(@NonNull final Location location, @NonNull final OnLocalityFound onLocalityFound) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String locality = null;

                try {
                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 10);

                    for (Address address : addresses) {
                        if (address.getLocality() != null) {
                            locality = address.getLocality();
                            break;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return locality;
            }

            @Override
            protected void onPostExecute(String locality) {
                if (locality == null) {
                    return;
                }

                Locality.this.locality = locality;

                onLocalityFound.onLocalityFound(locality);
            }
        }.execute();
    }
}
