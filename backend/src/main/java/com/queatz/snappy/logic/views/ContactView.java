package com.queatz.snappy.logic.views;

import com.google.cloud.datastore.Entity;

/**
 * Created by jacob on 5/9/16.
 */
public class ContactView extends LinkView {
    public ContactView(Entity contact) {
        super(contact);
    }
}
