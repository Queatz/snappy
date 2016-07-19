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
 * Created by jacob on 5/14/16.
 */
public class FollowerMine extends EarthControl {
    public FollowerMine(final EarthAs as) {
        super(as);
    }

    public Entity forPerson(Entity person, Entity isFollowingPerson) {
        QueryResults<Entity> results = use(EarthStore.class).query(
                StructuredQuery.PropertyFilter.eq(EarthField.KIND, EarthKind.FOLLOWER_KIND),
                StructuredQuery.PropertyFilter.eq(EarthField.SOURCE, person.key()),
                StructuredQuery.PropertyFilter.eq(EarthField.TARGET, isFollowingPerson.key())
        );

        if (results.hasNext()) {
            return results.next();
        } else {
            return null;
        }
    }

    public int countFollowers(Entity entity) {
        return use(EarthStore.class).count(EarthKind.FOLLOWER_KIND, EarthField.TARGET, entity.key());
    }
}
