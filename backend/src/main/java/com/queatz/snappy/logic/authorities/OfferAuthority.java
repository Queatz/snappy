package com.queatz.snappy.logic.authorities;

import com.queatz.earth.EarthField;
import com.queatz.earth.EarthRule;
import com.queatz.earth.EarthThing;
import com.queatz.earth.Authority;

/**
 * Created by jacob on 7/19/16.
 */
public class OfferAuthority implements Authority {
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

                // Only people can edit themselves
                return !entity.has(EarthField.SOURCE) ||
                        as.key().equals(entity.getKey(EarthField.SOURCE));
            default:
                return true;
        }
    }
}
