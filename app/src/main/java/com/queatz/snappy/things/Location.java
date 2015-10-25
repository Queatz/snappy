package com.queatz.snappy.things;

import android.net.Uri;

import com.google.gson.JsonObject;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.Index;
import io.realm.annotations.RealmClass;

/**
 * Created by jacob on 2/14/15.
 */

@RealmClass
public class Location extends RealmObject {
    @Index
    private String id;
    private String name;
    private String address;
    private double latitude;
    private double longitude;

    @Ignore
    private JsonObject json;
    @Ignore
    private String text;

    public JsonObject getJson() {
        JsonObject o = new JsonObject();
        o.addProperty("latitude", latitude);
        o.addProperty("longitude", longitude);
        o.addProperty("name", name);
        o.addProperty("address", address);

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
