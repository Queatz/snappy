package com.queatz.snappy.logic;

import com.google.cloud.datastore.Entity;

/**
 * The class that determines whether or not you can see something.
 */
public class EarthAuthority {
    public boolean authorize(Entity user, Entity entity, EarthRule rule) {
        return true;
    }
}
