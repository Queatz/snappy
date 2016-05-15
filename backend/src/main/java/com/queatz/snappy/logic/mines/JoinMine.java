package com.queatz.snappy.logic.mines;

import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StructuredQuery;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthKind;
import com.queatz.snappy.logic.EarthSingleton;
import com.queatz.snappy.logic.EarthStore;

/**
 * Created by jacob on 5/14/16.
 */
public class JoinMine {

    EarthStore earthStore = EarthSingleton.of(EarthStore.class);

    public Entity byPersonAndParty(Entity person, Entity party) {
        QueryResults<Entity> results = earthStore.query(
                StructuredQuery.PropertyFilter.eq(EarthField.KIND, EarthKind.JOIN_KIND),
                StructuredQuery.PropertyFilter.eq(EarthField.SOURCE, person.key()),
                StructuredQuery.PropertyFilter.eq(EarthField.TARGET, party.key())
        );

        if (results.hasNext()) {
            return results.next();
        } else {
            return null;
        }
    }
}
