package com.queatz.snappy.logic.views;

import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthThing;
import com.queatz.snappy.logic.EarthView;

/**
 * Created by jacob on 5/22/16.
 */
public class ResourceView extends CommonThingView {

    public ResourceView(EarthAs as, EarthThing resource) {
        this(as, resource, EarthView.DEEP);
    }

    public ResourceView(EarthAs as, EarthThing resource, EarthView view) {
        super(as, resource, view);
    }
}
