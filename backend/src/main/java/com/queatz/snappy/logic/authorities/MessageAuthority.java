package com.queatz.snappy.logic.authorities;

import com.google.cloud.datastore.Entity;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthRule;
import com.queatz.snappy.logic.concepts.Authority;

/**
 * Created by jacob on 7/19/16.
 */
public class MessageAuthority implements Authority {
    @Override
    public boolean authorize(Entity as, Entity entity, EarthRule rule) {
        switch (rule) {
            case ACCESS:
                // Only message senders / receivers can see message
                return !entity.contains(EarthField.SOURCE) ||
                        as.key().equals(entity.getKey(EarthField.SOURCE)) ||
                        as.key().equals(entity.getKey(EarthField.TARGET));
            case MODIFY:
                // Only message senders can edit messages
                return !entity.contains(EarthField.SOURCE) ||
                        as.key().equals(entity.getKey(EarthField.SOURCE));
            default:
                return true;
        }
    }
}
