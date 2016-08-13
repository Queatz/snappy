package com.queatz.snappy.logic.authorities;

import com.google.cloud.datastore.Entity;
import com.queatz.snappy.logic.EarthAuthority;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthRule;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.snappy.logic.concepts.Authority;

/**
 * Created by jacob on 7/19/16.
 */
public class ContactAuthority implements Authority {
    @Override
    public boolean authorize(Entity as, Entity entity, EarthRule rule) {
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
