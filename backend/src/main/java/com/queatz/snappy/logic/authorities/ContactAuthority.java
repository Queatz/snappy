package com.queatz.snappy.logic.authorities;

import com.queatz.snappy.logic.EarthRule;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.logic.concepts.Authority;

/**
 * Created by jacob on 7/19/16.
 */
public class ContactAuthority implements Authority {
    @Override
    public boolean authorize(EarthThing as, EarthThing entity, EarthRule rule) {
        switch (rule) {
            case ACCESS:
                // Anyone can see
                return true;
            case MODIFY:
                // Only owners of the thing can edit contacts related to the thing

                //Entity thing = use(EarthStore.class).get(entity.getKey(EarthField.SOURCE));

                //return use(EarthAuthority.class).authorize(as, thing, EarthRule.MODIFY);
            default:
                return true;
        }
    }
}
