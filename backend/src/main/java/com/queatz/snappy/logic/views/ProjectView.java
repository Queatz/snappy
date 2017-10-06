package com.queatz.snappy.logic.views;

import com.queatz.snappy.logic.EarthAs;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.logic.EarthView;

/**
 * Created by jacob on 5/23/16.
 */
public class ProjectView extends CommonThingView {

    public ProjectView(EarthAs as, EarthThing project) {
        this(as, project, EarthView.DEEP);
    }

    public ProjectView(EarthAs as, EarthThing project, EarthView view) {
        super(as, project, view);
    }
}
