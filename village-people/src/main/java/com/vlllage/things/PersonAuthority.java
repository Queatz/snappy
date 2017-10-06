package com.vlllage.things;

import com.queatz.earth.EarthRule;
import com.queatz.earth.EarthThing;

/**
 * Created by jacob on 7/19/16.
 */
public class PersonAuthority extends CommonThingAuthority {
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
                return as.key().equals(entity.key());
            default:
                return true;
        }
    }
}
