package com.queatz.snappy.logic.views;

import com.google.cloud.datastore.Entity;
import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthView;

/**
 * Created by jacob on 5/14/16.
 */
public class LocationView extends ExistenceView {

    String name;
    String address;

    public LocationView(EarthAs as, Entity location) {
        this(as, location, EarthView.DEEP);
    }

    public LocationView(EarthAs as, Entity location, EarthView view) {
        super(as, location, view);

        name = location.getString(EarthField.NAME);
        address = location.getString(EarthField.ADDRESS);
    }
}
