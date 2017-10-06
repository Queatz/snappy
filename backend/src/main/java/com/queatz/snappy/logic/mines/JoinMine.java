package com.queatz.snappy.logic.mines;

import com.google.common.collect.ImmutableMap;
import com.queatz.snappy.as.EarthAs;
import com.queatz.snappy.as.EarthControl;
import com.queatz.earth.EarthField;
import com.queatz.earth.EarthKind;
import com.queatz.earth.EarthStore;
import com.queatz.earth.EarthThing;

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
                        "x." + EarthField.SOURCE + " == @source and " +
                        "x." + EarthField.TARGET + " == @target",
                ImmutableMap.<String, Object>of(
                        "kind", EarthKind.JOIN_KIND,
                        "source", person.key().name(),
                        "target", party.key().name()
                ), 1);

        if (result.isEmpty()) {
            return null;
        } else {
            return result.get(0);
        }
    }
}
