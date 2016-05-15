package com.queatz.snappy.logic.mines;

import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StructuredQuery;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthKind;
import com.queatz.snappy.logic.EarthSingleton;
import com.queatz.snappy.logic.EarthStore;

/**
 * Created by jacob on 5/14/16.
 */
public class RecentMine {

    final EarthStore earthStore = EarthSingleton.of(EarthStore.class);

    public Entity byPerson(Entity person, Entity contact) {
        return byPerson(person.key(), contact.key());
    }

    public Entity byPerson(Key person, Key contact) {
        QueryResults<Entity> results = earthStore.query(
                StructuredQuery.PropertyFilter.eq(EarthField.KIND, EarthKind.RECENT_KIND),
                StructuredQuery.PropertyFilter.eq(EarthField.SOURCE, person),
                StructuredQuery.PropertyFilter.eq(EarthField.TARGET, contact)
        );

        if (results.hasNext()) {
            return results.next();
        } else {
            return null;
        }
    }
}
