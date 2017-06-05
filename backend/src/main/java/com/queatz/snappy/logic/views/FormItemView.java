package com.queatz.snappy.logic.views;

import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthThing;
import com.queatz.snappy.logic.EarthView;

/**
 * Created by jacob on 6/4/17.
 */

public class FormItemView extends LinkView {

    final String itemType;

    public FormItemView(EarthAs as, EarthThing link) {
        this(as, link, EarthView.DEEP);
    }

    public FormItemView(EarthAs as, EarthThing link, EarthView view) {
        super(as, link, view);
    }
}
