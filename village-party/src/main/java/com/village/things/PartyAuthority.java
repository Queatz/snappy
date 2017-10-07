package com.village.things;

import com.queatz.earth.Authority;
import com.queatz.earth.EarthField;
import com.queatz.earth.EarthRule;
import com.queatz.earth.EarthThing;

/**
 * Created by jacob on 8/22/16.
 */

public class PartyAuthority implements Authority {
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
                return !entity.has(EarthField.HOST) ||
                        as.key().equals(entity.getKey(EarthField.HOST));
        }

        return true;
    }
}
