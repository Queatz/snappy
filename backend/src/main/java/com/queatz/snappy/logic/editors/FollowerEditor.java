package com.queatz.snappy.logic.editors;

import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthControl;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthKind;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.snappy.logic.EarthThing;

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
