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

        name = thing.getString(EarthField.NAME);
        about = thing.getString(EarthField.ABOUT);
        photo = thing.getBoolean(EarthField.PHOTO);

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
