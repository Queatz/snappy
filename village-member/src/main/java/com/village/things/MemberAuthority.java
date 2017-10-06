package com.village.things;

import com.queatz.snappy.as.EarthAs;
import com.queatz.earth.EarthField;
import com.queatz.earth.EarthRule;
import com.queatz.earth.EarthStore;
import com.queatz.earth.EarthThing;
import com.queatz.earth.Authority;

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
