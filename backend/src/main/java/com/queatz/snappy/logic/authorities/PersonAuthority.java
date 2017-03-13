package com.queatz.snappy.logic.authorities;

import com.queatz.snappy.logic.EarthRule;
import com.queatz.snappy.logic.EarthThing;

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
                // Only people can edit themselves
                return as.key().equals(entity.key());
            default:
                return true;
        }
    }
}
