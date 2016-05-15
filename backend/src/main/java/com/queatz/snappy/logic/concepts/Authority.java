package com.queatz.snappy.logic.concepts;

import com.google.appengine.api.datastore.Entity;
import com.queatz.snappy.logic.EarthAs;

/**
 * Can I see you?
 */
public interface Authority {
    boolean authorized(Entity entity, EarthAs as);
}
