package com.queatz.snappy.logic.mines;

import com.google.common.collect.ImmutableMap;
import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthControl;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthKind;
import com.queatz.snappy.logic.EarthRef;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.snappy.logic.EarthThing;

import java.util.List;

/**
 * Created by jacob on 5/14/16.
 */
public class RecentMine extends EarthControl {
    public RecentMine(final EarthAs as) {
        super(as);
    }

    public List<EarthThing> forPerson(EarthThing person) {
        return use(EarthStore.class).query(
                EarthField.KIND + " == @kind and " +
                EarthField.SOURCE + " == @source_key",
                ImmutableMap.of(
                        "kind", EarthKind.RECENT_KIND,
                        "source_key", person.key()
                )
        );
    }

    public EarthThing byPerson(EarthThing person, EarthThing contact) {
        return byPerson(person.key(), contact.key());
    }

    public EarthThing byPerson(EarthRef person, EarthRef contact) {
        List<EarthThing> result = use(EarthStore.class).query(
                EarthField.KIND + " == @kind and " +
                        EarthField.SOURCE + " == @source_key " +
                        EarthField.TARGET + " == @target_key",
                ImmutableMap.<String, Object>of(
                        "kind", EarthKind.RECENT_KIND,
                        "source_key", person.name(),
                        "target_key", contact.name()
                )
        , 1);

        if (result.isEmpty()) {
            return null;
        } else {
            return result.get(0);
        }
    }
}
