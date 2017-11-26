package com.queatz.earth.query;

import com.queatz.earth.EarthQuery;
import com.queatz.snappy.as.EarthAs;
import com.queatz.snappy.as.EarthControl;

import static com.queatz.earth.EarthStore.DEFAULT_COLLECTION;

/**
 * Created by jacob on 8/21/17.
 */

public class EarthQueryNearFilter extends EarthControl {

    private String latitude;
    private String longitude;
    private String limit;

    public EarthQueryNearFilter(EarthAs as) {
        this(as, "@latitude", "@longitude", "@limit");
    }

    public EarthQueryNearFilter(EarthAs as, String latitude, String longitude, String limit) {
        super(as);
        this.latitude = latitude;
        this.longitude = longitude;
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
        return new EarthQuery(as)
                .in("near(" + DEFAULT_COLLECTION + ", " + latitude + ", " + longitude + ", " + limit + ")")
                .aql();
    }
}
