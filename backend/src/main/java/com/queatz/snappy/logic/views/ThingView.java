package com.queatz.snappy.logic.views;

import com.google.cloud.datastore.Entity;
import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthView;

/**
 * Created by jacob on 4/3/16.
 */
public class ThingView extends ExistenceView {

    final String name;
    final String about;
    final boolean photo;

    public ThingView(EarthAs as, Entity hub) {
        this(as, hub, EarthView.DEEP);
    }

    public ThingView(EarthAs as, Entity hub, EarthView view) {
        super(as, hub, view);

        name = hub.getString(EarthField.NAME);
        about = hub.getString(EarthField.ABOUT);
        photo = hub.getBoolean(EarthField.PHOTO);
    }
}
