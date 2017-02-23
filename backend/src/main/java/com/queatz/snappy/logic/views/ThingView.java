package com.queatz.snappy.logic.views;

import com.google.cloud.datastore.Entity;
import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthKind;
import com.queatz.snappy.logic.EarthView;
import com.queatz.snappy.service.ImageQueue;

/**
 * Created by jacob on 4/3/16.
 */
public class ThingView extends ExistenceView {

    final String name;
    final String about;
    final boolean photo;
    final String placeholder;
    final Float aspect;

    public ThingView(EarthAs as, Entity thing) {
        this(as, thing, EarthView.DEEP);
    }

    public ThingView(EarthAs as, Entity thing, EarthView view) {
        super(as, thing, view);

        if (thing.contains(EarthField.NAME)) {
            name = thing.getString(EarthField.NAME);
        } else {
            name = null;
        }

        photo = thing.contains(EarthField.PHOTO) && thing.getBoolean(EarthField.PHOTO);

        if (thing.contains(EarthField.ABOUT)) {
            about = thing.getString(EarthField.ABOUT);
        } else {
            about = null;
        }

        String spacer = null;

        if (photo) {
            if (thing.contains(EarthField.PLACEHOLDER)) {
                spacer = thing.getString(EarthField.PLACEHOLDER);
            } else {
                ImageQueue.getService().enqueue(thing.key().name());
            }
        }

        placeholder = spacer;

        if (placeholder != null && thing.contains(EarthField.ASPECT_RATIO)) {
            aspect = (float) thing.getDouble(EarthField.ASPECT_RATIO);
        } else {
            aspect = null;
        }
    }
}
