package com.queatz.snappy.logic.views;

import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthGeo;
import com.queatz.snappy.logic.EarthThing;
import com.queatz.snappy.logic.EarthView;

/**
 * Created by jacob on 5/14/16.
 */
public class LocationView extends ExistenceView {

    final String name;
    final String address;
    final EarthGeo geo;

    public LocationView(EarthAs as, EarthThing location) {
        this(as, location, EarthView.DEEP);
    }

    public LocationView(EarthAs as, EarthThing location, EarthView view) {
        super(as, location, view);

        name = location.getString(EarthField.NAME);
        address = location.getString(EarthField.ADDRESS);

        geo = new EarthGeo(
                (float) location.getGeo(EarthField.GEO).getLatitude(),
                (float) location.getGeo(EarthField.GEO).getLongitude()
        );
    }
}
