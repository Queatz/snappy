package com.village.things;

import com.queatz.earth.EarthField;
import com.queatz.earth.EarthRule;
import com.queatz.earth.EarthThing;
import com.queatz.earth.Authority;

/**
 * Created by jacob on 5/9/16.
 */
public class UpdateAuthority implements Authority {
    @Override
    public boolean authorize(EarthThing as, EarthThing entity, EarthRule rule) {
        switch (rule) {
            case ACCESS:
                // Anyone can see
                return true;
            case MODIFY:
                if (as == null) {
                    return false;
                }

                // Only people can edit their updates
                // XXX todo owners of TARGET should be able to remove
                return !entity.has(EarthField.SOURCE) ||
                        as.key().equals(entity.getKey(EarthField.SOURCE));
            default:
                return true;
        }
    }
}