package com.queatz.snappy.logic.authorities;

import com.queatz.snappy.logic.EarthRule;
import com.queatz.snappy.logic.EarthThing;
import com.queatz.snappy.logic.concepts.Authority;

/**
 * Created by jacob on 4/9/17.
 */

public class MemberAuthority implements Authority {
    @Override
    public boolean authorize(EarthThing as, EarthThing entity, EarthRule rule) {
        switch (rule) {
            case ACCESS:
                return true;
            case MODIFY:
                if (as == null) {
                    return false;
                }
        }

        return true;
    }
}
