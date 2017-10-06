package com.queatz.snappy.logic.views;

import com.queatz.snappy.as.EarthAs;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.view.EarthView;
import com.village.things.LinkView;

/**
 * Created by jacob on 5/9/16.
 */
public class ContactView extends LinkView {

    public ContactView(EarthAs as, EarthThing contact) {
        this(as, contact, EarthView.DEEP);
    }

    public ContactView(EarthAs as, EarthThing contact, EarthView view) {
        super(as, contact, view);
    }
}
