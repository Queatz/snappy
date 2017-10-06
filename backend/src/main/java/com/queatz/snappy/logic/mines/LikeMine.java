package com.queatz.snappy.logic.mines;

import com.google.common.collect.ImmutableMap;
import com.queatz.snappy.api.EarthAs;
import com.queatz.snappy.api.EarthControl;
import com.queatz.earth.EarthField;
import com.queatz.earth.EarthKind;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.earth.EarthThing;

import java.util.List;

/**
 * Created by jacob on 8/24/16.
 */

public class LikeMine extends EarthControl {
    public LikeMine(final EarthAs as) {
        super(as);
    }

    public EarthThing getLike(EarthThing person, EarthThing thing) {
        List<EarthThing> result = use(EarthStore.class).query(
                "x." + EarthField.KIND + " == @kind and " +
                        "x." + EarthField.SOURCE + " == @source_key and " +
                        "x." + EarthField.TARGET + " == @target_key",
                ImmutableMap.<String, Object>of(
                        "kind", EarthKind.LIKE_KIND,
                        "source_key", person.key().name(),
                        "target_key", thing.key().name()
                ), 1);

        if (result.isEmpty()) {
            return null;
        } else {
            return result.get(0);
        }
    }

    public int countLikers(EarthThing entity) {
        return use(EarthStore.class).count(EarthKind.LIKE_KIND, EarthField.TARGET, entity.key());
    }
}
