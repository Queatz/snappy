package com.queatz.snappy.logic.mines;

import com.google.cloud.datastore.StructuredQuery;
import com.google.common.collect.Lists;
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
        return Lists.newArrayList(use(EarthStore.class).query(
                StructuredQuery.PropertyFilter.eq(EarthField.KIND, EarthKind.RECENT_KIND),
                StructuredQuery.PropertyFilter.eq(EarthField.SOURCE, person.key())
        ));
    }

    public EarthThing byPerson(EarthThing person, EarthThing contact) {
        return byPerson(person.key(), contact.key());
    }

    public EarthThing byPerson(EarthRef person, EarthRef contact) {
        List<EarthThing> results = use(EarthStore.class).queryLimited(1,
                StructuredQuery.PropertyFilter.eq(EarthField.KIND, EarthKind.RECENT_KIND),
                StructuredQuery.PropertyFilter.eq(EarthField.SOURCE, person),
                StructuredQuery.PropertyFilter.eq(EarthField.TARGET, contact)
        );

        if (results.isEmpty()) {
            return null;
        } else {
            return results.get(0);
        }
    }
}
