package com.queatz.earth;


import com.queatz.snappy.as.EarthAs;
import com.queatz.snappy.as.EarthControl;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Created by jacob on 9/4/17.
 */

public class EarthVisibility extends EarthControl {
    public EarthVisibility(@NotNull EarthAs as) {
        super(as);
    }

    public void setHidden(EarthThing thing, boolean hidden) {
        EarthStore earthStore = use(EarthStore.class);

        earthStore.save(earthStore.edit(thing)
            .set(EarthField.HIDDEN, hidden));
    }

    public void setVisibility(EarthThing thing, Map<String, Boolean> clubs) {
        clubs.forEach((String id, Boolean visible) -> {
            if (visible) {
                use(EarthStore.class).addToClub(thing, use(EarthStore.class).get(id));
            } else {
                use(EarthStore.class).removeFromClub(thing, use(EarthStore.class).get(id));
            }
        });
    }
}
