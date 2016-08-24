package com.queatz.snappy.logic.authorities;

import com.google.cloud.datastore.Entity;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthRule;
import com.queatz.snappy.logic.concepts.Authority;

/**
 * Created by jacob on 8/22/16.
 */

public class PartyAuthority implements Authority {
    @Override
    public boolean authorize(Entity as, Entity entity, EarthRule rule) {
        switch (rule) {
            case ACCESS:
                return true;
            case MODIFY:
                // XXX todo if as.getUser() in entity.getContacts()
                // XXX todo no source === just being created, so it's ok, but is it?
                return !entity.contains(EarthField.HOST) ||
                        as.key().equals(entity.getKey(EarthField.HOST));
        }

        return true;
    }
}
