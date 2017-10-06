package com.queatz.snappy.logic.views;

import com.queatz.snappy.as.EarthAs;
import com.queatz.earth.EarthField;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.view.EarthView;

/**
 * Created by jacob on 6/4/17.
 */

public class FormItemView extends LinkView {

    final String type;

    public FormItemView(EarthAs as, EarthThing link) {
        this(as, link, EarthView.DEEP);
    }

    public FormItemView(EarthAs as, EarthThing link, EarthView view) {
        super(as, link, view);

        type = link.getString(EarthField.TYPE);
    }
}
