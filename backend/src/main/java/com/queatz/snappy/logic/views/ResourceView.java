package com.queatz.snappy.logic.views;

import com.google.cloud.datastore.Entity;
import com.queatz.snappy.logic.EarthView;

/**
 * Created by jacob on 5/22/16.
 */
public class ResourceView extends CommonThingView {

    public ResourceView(Entity resource) {
        this(resource, EarthView.DEEP);
    }

    public ResourceView(Entity resource, EarthView view) {
        super(resource, view);
    }
}
