package com.queatz.snappy.logic.views;

import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthThing;
import com.queatz.snappy.logic.EarthView;


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
