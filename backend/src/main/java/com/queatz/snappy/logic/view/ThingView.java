package com.queatz.snappy.logic.view;

import com.google.cloud.datastore.Entity;
import com.queatz.snappy.logic.EarthField;

/**
 * Created by jacob on 4/3/16.
 */
public class ThingView extends ExistenceView {

    final String name;
    final String about;
    final boolean photo;

    public ThingView(Entity hub) {
        super(hub);

        name = hub.getString(EarthField.NAME);
        about = hub.getString(EarthField.ABOUT);
        photo = hub.getBoolean(EarthField.PHOTO);
    }
}
