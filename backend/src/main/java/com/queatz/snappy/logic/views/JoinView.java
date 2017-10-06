package com.queatz.snappy.logic.views;

import com.queatz.snappy.logic.EarthAs;
import com.queatz.earth.EarthField;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.logic.EarthView;

/**
 * Created by jacob on 5/14/16.
 */
public class JoinView extends LinkView {

    final String status;

    public JoinView(EarthAs as, EarthThing join) {
        this(as, join, EarthView.DEEP);
    }

    public JoinView(EarthAs as, EarthThing join, EarthView view) {
        super(as, join, view);
        status = join.getString(EarthField.STATUS);
    }
}
