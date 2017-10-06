package com.queatz.snappy.logic.authorities;

import com.queatz.earth.EarthField;
import com.queatz.earth.EarthRule;
import com.queatz.earth.EarthThing;
import com.queatz.earth.Authority;

/**
 * Created by jacob on 7/9/16.
 */
public class CommonThingAuthority implements Authority {
    @Override
    public boolean authorize(EarthThing as, EarthThing entity, EarthRule rule) {
        switch (rule) {
            case ACCESS:
                return true;
            case MODIFY:
                if (as == null) {
                    return false;
                }

                // XXX todo if as.getUser() in entity.getContacts()
                // XXX todo no source === just being created, so it's ok, but is it?
                return !entity.has(EarthField.SOURCE) ||
                        as.key().equals(entity.getKey(EarthField.SOURCE));
        }

        return true;
    }
}
