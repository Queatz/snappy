package com.queatz.snappy.logic.views;

import com.google.cloud.datastore.Entity;
import com.queatz.snappy.logic.EarthView;

/**
 * Created by jacob on 5/9/16.
 */
public class ContactView extends LinkView {

    public ContactView(Entity contact) {
        this(contact, EarthView.DEEP);
    }

    public ContactView(Entity contact, EarthView view) {
        super(contact, view);
    }
}
