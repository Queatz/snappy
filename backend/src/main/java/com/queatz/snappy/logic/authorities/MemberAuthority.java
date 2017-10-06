package com.queatz.snappy.logic.authorities;

import com.queatz.snappy.api.EarthAs;
import com.queatz.earth.EarthField;
import com.queatz.snappy.logic.EarthRule;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.earth.EarthThing;
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
                EarthStore earthStore = earthAs.s(EarthStore.class);

                if (as != null) {
                    EarthThing owner = earthStore.ownerOf(entity);

                    if (owner != null && owner.id().equals(as.id())) {
                        return true;
                    } else if (entity.has(EarthField.TARGET)) {
                        EarthThing target = earthStore.get(entity.getString(EarthField.TARGET));

                        if (target != null && target.key().name().equals(as.id())) {
                            return true;
                        }

                        EarthThing ownerOfTarget = earthStore.ownerOf(target);
                        return ownerOfTarget != null && ownerOfTarget.id().equals(as.id());
                    }
                }
        }

        return false;
    }
}
