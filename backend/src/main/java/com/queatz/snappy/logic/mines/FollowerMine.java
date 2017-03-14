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
public class FollowerMine extends EarthControl {
    public FollowerMine(final EarthAs as) {
        super(as);
    }

    public EarthThing getFollower(EarthThing person, EarthThing isFollowingPerson) {
        List<EarthThing> result = use(EarthStore.class).query(
                EarthField.KIND + " == @kind and " +
                        EarthField.SOURCE + " == @source_key " +
                        EarthField.TARGET + " == @target_key",
                ImmutableMap.of(
                        "kind", EarthKind.FOLLOWER_KIND,
                        "source_key", person.key().name(),
                        "target_key", isFollowingPerson.key().name()
                ), 1);

        if (result.isEmpty()) {
            return null;
        } else {
            return result.get(0);
        }
    }

    public int countFollowers(EarthThing entity) {
        return use(EarthStore.class).count(EarthKind.FOLLOWER_KIND, EarthField.TARGET, entity.key());
    }
}
