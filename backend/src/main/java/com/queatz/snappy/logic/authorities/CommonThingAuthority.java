package com.queatz.snappy.logic.authorities;

import com.google.appengine.api.datastore.Entity;
import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthRule;
import com.queatz.snappy.logic.concepts.Authority;

/**
 * Created by jacob on 7/9/16.
 */
public class CommonThingAuthority implements Authority {
    @Override
    public boolean authorized(Entity entity, EarthAs as, EarthRule rule) {
        switch (rule) {
            case ACCESS:
                // if as.getUser().getKeys() overlaps entity.getKeys()
                break;
            case MODIFY:
                // if as.getUser() in entity.getContacts()
                break;
        }

        return true;
    }
}
