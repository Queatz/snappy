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
                    EarthThing owner = earthStore.ownerOf(entity);

                    if (owner != null && owner.id().equals(as.id())) {
                        return true;
                    } if (entity.has(EarthField.TARGET)) {
                        EarthThing target = earthStore.get(entity.getString(EarthField.TARGET));

                        if (target != null && target.key().name().equals(as.id())) {
                            return true;
                        }

                        EarthThing ownerOfTarget = earthStore.ownerOf(target);
                        return ownerOfTarget != null && ownerOfTarget.id().equals(as.id());
                    }
                } else {
                    return false;
                }
        }

        return false;
    }
}
