package com.village.things;

import com.queatz.earth.Authority;
import com.queatz.earth.EarthField;
import com.queatz.earth.EarthRule;
import com.queatz.earth.EarthStore;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.as.EarthAs;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ActionAuthority implements Authority {

    @Override
    public boolean authorize(@Nullable EarthThing as, @NotNull EarthThing entity, @NotNull EarthRule rule) {
        switch (rule) {
            case ACCESS:
                // Anyone can see
                return true;
            case MODIFY:
                // Only owners of the thing can edit contacts related to the thing

                if (!entity.has(EarthField.SOURCE)) {
                    return true;
                }

                EarthAs ass = new EarthAs();
                EarthThing thing = ass.s(EarthStore.class).get(entity.getKey(EarthField.SOURCE));
                EarthThing owner = ass.s(EarthStore.class).ownerOf(thing);

                return thing != null && as != null && owner != null && owner.id().equals(as.id());
            default:
                return true;
        }
    }
}
