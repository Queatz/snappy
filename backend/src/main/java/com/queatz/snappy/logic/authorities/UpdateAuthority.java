package com.queatz.snappy.logic.authorities;

import com.google.cloud.datastore.Entity;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthRule;
import com.queatz.snappy.logic.concepts.Authority;

/**
 * Created by jacob on 5/9/16.
 */
public class UpdateAuthority implements Authority {
    @Override
    public boolean authorize(Entity as, Entity entity, EarthRule rule) {
        switch (rule) {
            case ACCESS:
                // Anyone can see
                return true;
            case MODIFY:
                // Only people can edit their updates
                // XXX todo owners of TARGET should be able to remove
                return as.getKey(EarthField.SOURCE).equals(entity.key());
            default:
                return true;
        }
    }
}
