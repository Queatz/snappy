package com.queatz.snappy.logic.views;

import com.google.cloud.datastore.Entity;
import com.queatz.snappy.logic.EarthView;

/**
 * Created by jacob on 5/23/16.
 */
public class ProjectView extends CommonThingView {

    public ProjectView(Entity resource) {
        this(resource, EarthView.DEEP);
    }

    public ProjectView(Entity resource, EarthView view) {
        super(resource, view);
    }
}
