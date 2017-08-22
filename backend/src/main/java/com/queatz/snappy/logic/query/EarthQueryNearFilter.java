package com.queatz.snappy.logic.query;

import static com.queatz.snappy.logic.EarthStore.DEFAULT_COLLECTION;

/**
 * Created by jacob on 8/21/17.
 */

public class EarthQueryNearFilter {

    private String longitude;
    private String latitude;
    private String limit;

    public EarthQueryNearFilter(String longitude, String latitude, String limit) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.limit = limit;
    }

    public String getLongitude() {
        return longitude;
    }

    public EarthQueryNearFilter setLongitude(String longitude) {
        this.longitude = longitude;
        return this;
    }

    public String getLatitude() {
        return latitude;
    }

    public EarthQueryNearFilter setLatitude(String latitude) {
        this.latitude = latitude;
        return this;
    }

    public String getLimit() {
        return limit;
    }

    public EarthQueryNearFilter setLimit(String limit) {
        this.limit = limit;
        return this;
    }

    public String aql() {
        return "for x in near(" + DEFAULT_COLLECTION + ", @latitude, @longitude, @limit) return x";
    }
}
