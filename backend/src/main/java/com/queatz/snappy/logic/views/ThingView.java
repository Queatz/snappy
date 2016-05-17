package com.queatz.snappy.logic.views;

import com.google.cloud.datastore.Entity;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthView;

/**
 * Created by jacob on 4/3/16.
 */
public class ThingView extends ExistenceView {

    final String name;
    final String about;
    final boolean photo;

    public ThingView(Entity hub) {
        this(hub, EarthView.DEEP);
    }

    public ThingView(Entity hub, EarthView view) {
        super(hub, view);

        name = hub.getString(EarthField.NAME);
        about = hub.getString(EarthField.ABOUT);
        photo = hub.getBoolean(EarthField.PHOTO);
    }
}
