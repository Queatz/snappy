package com.queatz.snappy.logic.editors;

import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthControl;
import com.queatz.earth.EarthField;
import com.queatz.earth.EarthKind;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.earth.EarthThing;

/**
 * Created by jacob on 5/8/16.
 */
public class LikeEditor extends EarthControl {
    private final EarthStore earthStore;

    public LikeEditor(final EarthAs as) {
        super(as);

        earthStore = use(EarthStore.class);
    }

    public EarthThing newLike(EarthThing person, EarthThing thing) {
        return earthStore.save(earthStore.edit(earthStore.create(EarthKind.LIKE_KIND))
                .set(EarthField.SOURCE, person.key())
                .set(EarthField.TARGET, thing.key()));
    }
}
