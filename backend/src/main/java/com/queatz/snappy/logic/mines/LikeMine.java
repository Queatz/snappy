package com.queatz.snappy.logic.mines;

import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StructuredQuery;
import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthControl;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthKind;
import com.queatz.snappy.logic.EarthStore;

/**
 * Created by jacob on 8/24/16.
 */

public class LikeMine extends EarthControl {
    public LikeMine(final EarthAs as) {
        super(as);
    }

    public Entity getLike(Entity person, Entity thing) {
        QueryResults<Entity> results = use(EarthStore.class).query(
                StructuredQuery.PropertyFilter.eq(EarthField.KIND, EarthKind.LIKE_KIND),
                StructuredQuery.PropertyFilter.eq(EarthField.SOURCE, person.key()),
                StructuredQuery.PropertyFilter.eq(EarthField.TARGET, thing.key())
        );

        if (results.hasNext()) {
            return results.next();
        } else {
            return null;
        }
    }

    public int countLikers(Entity entity) {
        return use(EarthStore.class).count(EarthKind.LIKE_KIND, EarthField.TARGET, entity.key());
    }
}
