package com.queatz.snappy.logic.authorities;

import com.queatz.earth.EarthField;
import com.queatz.earth.EarthRule;
import com.queatz.earth.EarthThing;
import com.queatz.earth.Authority;

/**
 * Created by jacob on 7/19/16.
 */
public class MessageAuthority implements Authority {
    @Override
    public boolean authorize(EarthThing as, EarthThing entity, EarthRule rule) {
        switch (rule) {
            case ACCESS:
                if (as == null) {
                    return false;
                }

                // Only message senders / receivers can see message
                return !entity.has(EarthField.SOURCE) ||
                        as.key().equals(entity.getKey(EarthField.SOURCE)) ||
                        as.key().equals(entity.getKey(EarthField.TARGET));
            case MODIFY:
                if (as == null) {
                    return false;
                }

                // Only message senders can edit messages
                return !entity.has(EarthField.SOURCE) ||
                        as.key().equals(entity.getKey(EarthField.SOURCE));
            default:
                return true;
        }
    }
}
