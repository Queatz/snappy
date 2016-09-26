package com.queatz.snappy.logic.mines;

import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StructuredQuery;
import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthControl;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthKind;
import com.queatz.snappy.logic.EarthStore;

import java.util.List;

/**
 * Created by jacob on 8/24/16.
 */

public class LikeMine extends EarthControl {
    public LikeMine(final EarthAs as) {
        super(as);
    }

    public Entity getLike(Entity person, Entity thing) {
        List<Entity> results = use(EarthStore.class).query(
                StructuredQuery.PropertyFilter.eq(EarthField.KIND, EarthKind.LIKE_KIND),
                StructuredQuery.PropertyFilter.eq(EarthField.SOURCE, person.key()),
                StructuredQuery.PropertyFilter.eq(EarthField.TARGET, thing.key())
        );

        if (results.isEmpty()) {
            return null;
        } else {
            return results.get(0);
        }
    }

    public int countLikers(Entity entity) {
        return use(EarthStore.class).count(EarthKind.LIKE_KIND, EarthField.TARGET, entity.key());
    }
}
