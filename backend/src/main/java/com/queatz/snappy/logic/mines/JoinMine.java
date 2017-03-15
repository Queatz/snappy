package com.queatz.snappy.logic.mines;

import com.google.common.collect.ImmutableMap;
import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthControl;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthKind;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.snappy.logic.EarthThing;

import java.util.List;

/**
 * Created by jacob on 5/14/16.
 */
public class JoinMine extends EarthControl {
    public JoinMine(final EarthAs as) {
        super(as);
    }

    public EarthThing byPersonAndParty(EarthThing person, EarthThing party) {
        List<EarthThing> result = use(EarthStore.class).query(
                "x." + EarthField.KIND + " == @kind and " +
                        "x." + EarthField.SOURCE + " == @source_key " +
                        "x." + EarthField.TARGET + " == @target_key",
                ImmutableMap.<String, Object>of(
                        "kind", EarthKind.JOIN_KIND,
                        "source_key", person.key().name(),
                        "target_key", party.key().name()
                ), 1);

        if (result.isEmpty()) {
            return null;
        } else {
            return result.get(0);
        }
    }
}
