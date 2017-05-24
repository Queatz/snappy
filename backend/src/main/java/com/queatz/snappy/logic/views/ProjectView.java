package com.queatz.snappy.logic.views;

import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthThing;
import com.queatz.snappy.logic.EarthView;

import java.util.Date;

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
