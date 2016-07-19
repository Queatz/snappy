package com.queatz.snappy.logic.views;

import com.google.cloud.datastore.Entity;
import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthView;

/**
 * Created by jacob on 5/9/16.
 */
public class ContactView extends LinkView {

    public ContactView(EarthAs as, Entity contact) {
        this(as, contact, EarthView.DEEP);
    }

    public ContactView(EarthAs as, Entity contact, EarthView view) {
        super(as, contact, view);
    }
}
