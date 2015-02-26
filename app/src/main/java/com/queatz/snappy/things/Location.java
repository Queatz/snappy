package com.queatz.snappy.things;

import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

import io.realm.RealmObject;
import io.realm.annotations.Index;

/**
 * Created by jacob on 2/14/15.
 */
public class Location extends RealmObject implements Thing {
    @Index
    private String id;
    private String name;
    private String address;
    private double latitude;
    private double longitude;

    public JSONObject getJson() {
        JSONObject o = new JSONObject();

        try {
            o.put("latitude", latitude);
            o.put("longitude", longitude);
            o.put("name", name);
            o.put("address", address);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        return o;
    }

    public String getText() {
        return getLatitude() + "," + getLongitude() + "(" + Uri.encode(getName()) + ")";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
