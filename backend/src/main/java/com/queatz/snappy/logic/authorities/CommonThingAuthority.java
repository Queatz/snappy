package com.queatz.snappy.logic.authorities;

import com.google.cloud.datastore.Entity;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthRule;
import com.queatz.snappy.logic.concepts.Authority;

/**
 * Created by jacob on 7/9/16.
 */
public class CommonThingAuthority implements Authority {
    @Override
    public boolean authorize(Entity as, Entity entity, EarthRule rule) {
        switch (rule) {
            case ACCESS:
                return true;
            case MODIFY:
                // XXX todo if as.getUser() in entity.getContacts()
                return as.key().equals(entity.getKey(EarthField.SOURCE));
        }

        return true;
    }
}
