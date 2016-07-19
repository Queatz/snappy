package com.queatz.snappy.logic;

import com.google.cloud.datastore.Entity;

/**
 * The class that determines whether or not you can see something.
 */
public class EarthAuthority extends EarthControl {
    public EarthAuthority(final EarthAs as) {
        super(as);
    }

    public boolean authorize(Entity entity, EarthRule rule) {
        return as == null || getUser() != null; // XXX TODO call kind authorizer rulers map
    }
}
