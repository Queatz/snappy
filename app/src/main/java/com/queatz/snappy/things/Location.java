package com.queatz.snappy.things;

import io.realm.RealmObject;
import io.realm.annotations.Index;

/**
 * Created by jacob on 2/14/15.
 */
public class Location extends RealmObject implements Thing {
    @Index
    private String id;
    private String name;
    private double latitude;
    private double longitude;

    public String getText() {
        return getLatitude() + "," + getLongitude() + "(" + getName() + ")";
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
