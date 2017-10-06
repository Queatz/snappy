package com.village.things;

import com.queatz.snappy.as.EarthAs;
import com.queatz.snappy.as.EarthControl;
import com.queatz.earth.EarthField;
import com.queatz.earth.EarthKind;
import com.queatz.earth.EarthStore;
import com.queatz.earth.EarthThing;

/**
 * Created by jacob on 5/8/16.
 */
public class FollowerEditor extends EarthControl {
    private final EarthStore earthStore;

    public FollowerEditor(final EarthAs as) {
        super(as);

        earthStore = use(EarthStore.class);
    }

    public EarthThing newFollower(EarthThing person, EarthThing thing) {
        return earthStore.save(earthStore.edit(earthStore.create(EarthKind.FOLLOWER_KIND))
                .set(EarthField.SOURCE, person.key())
                .set(EarthField.TARGET, thing.key()));
    }
}
