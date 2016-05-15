package com.queatz.snappy.logic.authorities;

import com.google.appengine.api.datastore.Entity;
import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.concepts.Authority;

/**
 * Created by jacob on 5/9/16.
 */
public class UpdateAuthority implements Authority {
    @Override
    public boolean authorized(Entity entity, EarthAs as) {
        return true; // TODO actually return EarthAuthority(entity.target, as)
    }
}
