package com.queatz.snappy.logic.mines;

import com.google.cloud.datastore.StructuredQuery;
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
        List<EarthThing> results = use(EarthStore.class).queryLimited(1,
                StructuredQuery.PropertyFilter.eq(EarthField.KIND, EarthKind.JOIN_KIND),
                StructuredQuery.PropertyFilter.eq(EarthField.SOURCE, person.key()),
                StructuredQuery.PropertyFilter.eq(EarthField.TARGET, party.key())
        );

        if (results.isEmpty()) {
            return null;
        } else {
            return results.get(0);
        }
    }
}
