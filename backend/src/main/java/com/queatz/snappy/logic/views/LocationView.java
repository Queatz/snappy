package com.queatz.snappy.logic.views;

import com.queatz.snappy.as.EarthAs;
import com.queatz.earth.EarthField;
import com.queatz.snappy.shared.earth.EarthGeo;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.view.EarthView;
import com.village.things.ExistenceView;

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
