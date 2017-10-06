package com.queatz.snappy.logic.views;

import com.queatz.snappy.as.EarthAs;
import com.queatz.earth.EarthField;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.view.EarthView;
import com.village.things.CommonThingView;


/**
 * Created by jacob on 6/4/17.
 */

public class FormView extends CommonThingView {

    private final String data;

    public FormView(EarthAs as, EarthThing thing) {
        this(as, thing, EarthView.DEEP);
    }

    public FormView(EarthAs as, EarthThing thing, EarthView view) {
        super(as, thing, view);
        this.data = thing.getString(EarthField.DATA);
    }
}
