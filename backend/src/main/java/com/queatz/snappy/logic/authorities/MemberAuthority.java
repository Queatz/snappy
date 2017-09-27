package com.queatz.snappy.logic.authorities;

import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthRule;
import com.queatz.snappy.logic.EarthStore;
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
                EarthAs earthAs = new EarthAs();

                if (as != null) {
                    EarthStore earthStore = earthAs.s(EarthStore.class);

                    if (entity.has(EarthField.TARGET)) {
                        EarthThing ownerOfTarget = earthStore.ownerOf(earthStore.get(entity.getString(EarthField.TARGET)));
                        return ownerOfTarget != null && ownerOfTarget.id().equals(as.id());
                    } else {
                        EarthThing owner = earthStore.ownerOf(entity);
                        return owner != null && owner.id().equals(as.id());
                    }
                } else {
                    return false;
                }
        }

        return false;
    }
}
