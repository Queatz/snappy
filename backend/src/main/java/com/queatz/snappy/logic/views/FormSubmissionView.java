package com.queatz.snappy.logic.views;

import com.queatz.snappy.api.EarthAs;
import com.queatz.earth.EarthField;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.logic.EarthView;

/**
 * Created by jacob on 6/4/17.
 */

public class FormSubmissionView extends LinkView {
    private final String data;

    public FormSubmissionView(EarthAs as, EarthThing link) {
        this(as, link, EarthView.DEEP);
    }

    public FormSubmissionView(EarthAs as, EarthThing link, EarthView view) {
        super(as, link, view);
        this.data = link.getString(EarthField.DATA);
    }
}
