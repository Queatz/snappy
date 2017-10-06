package com.village.things;

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
public class FollowerMine extends EarthControl {
    public FollowerMine(final EarthAs as) {
        super(as);
    }

    public EarthThing getFollower(EarthThing person, EarthThing isFollowingPerson) {
        List<EarthThing> result = use(EarthStore.class).query(
                "x." + EarthField.KIND + " == @kind and " +
                        "x." + EarthField.SOURCE + " == @source_key and " +
                        "x." + EarthField.TARGET + " == @target_key",
                ImmutableMap.<String, Object>of(
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

    public int countFollowing(EarthThing entity) {
        return use(EarthStore.class).count(EarthKind.FOLLOWER_KIND, EarthField.SOURCE, entity.key());
    }
}
