package com.queatz.snappy.logic.views;

import com.google.cloud.datastore.Entity;
import com.queatz.snappy.logic.EarthField;

/**
 * Created by jacob on 5/14/16.
 */
public class LocationView extends ExistenceView {

    String name;
    String address;

    public LocationView(Entity location) {
        super(location);

        name = location.getString(EarthField.NAME);
        address = location.getString(EarthField.ADDRESS);
    }
}
