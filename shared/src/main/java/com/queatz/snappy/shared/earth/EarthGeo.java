package com.queatz.snappy.shared.earth;

/**
 * Created by jacob on 3/12/17.
 */

public class EarthGeo {

    private double latitude;
    private double longitude;

    public EarthGeo() {}

    public EarthGeo(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public static EarthGeo of(double latitude, double longitude) {
        return new EarthGeo(latitude, longitude);
    }

    public double getLatitude() {
        return latitude;
    }

    public EarthGeo setLatitude(double latitude) {
        this.latitude = latitude;
        return this;
    }

    public double getLongitude() {
        return longitude;
    }

    public EarthGeo setLongitude(double longitude) {
        this.longitude = longitude;
        return this;
    }
}
