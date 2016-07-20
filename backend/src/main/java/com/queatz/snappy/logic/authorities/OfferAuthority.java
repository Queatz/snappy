package com.queatz.snappy.logic.authorities;

import com.google.cloud.datastore.Entity;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthRule;
import com.queatz.snappy.logic.concepts.Authority;

/**
 * Created by jacob on 7/19/16.
 */
public class OfferAuthority implements Authority {
    @Override
    public boolean authorize(Entity as, Entity entity, EarthRule rule) {
        switch (rule) {
            case ACCESS:
                // Anyone can see
                return true;
            case MODIFY:
                // Only people can edit themselves
                return as.getKey(EarthField.SOURCE).equals(entity.key());
            default:
                return true;
        }
    }
}
