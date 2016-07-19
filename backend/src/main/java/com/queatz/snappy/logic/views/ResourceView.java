package com.queatz.snappy.logic.views;

import com.google.cloud.datastore.Entity;
import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthView;

/**
 * Created by jacob on 5/22/16.
 */
public class ResourceView extends CommonThingView {

    public ResourceView(EarthAs as, Entity resource) {
        this(as, resource, EarthView.DEEP);
    }

    public ResourceView(EarthAs as, Entity resource, EarthView view) {
        super(as, resource, view);
    }
}
