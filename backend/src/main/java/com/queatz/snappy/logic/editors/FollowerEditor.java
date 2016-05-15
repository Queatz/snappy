package com.queatz.snappy.logic.editors;

import com.google.cloud.datastore.Entity;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthKind;
import com.queatz.snappy.logic.EarthSingleton;
import com.queatz.snappy.logic.EarthStore;

/**
 * Created by jacob on 5/8/16.
 */
public class FollowerEditor {
    private final EarthStore earthStore = EarthSingleton.of(EarthStore.class);

    public Entity newFollower(Entity person, Entity thing) {
        return earthStore.save(earthStore.edit(earthStore.create(EarthKind.FOLLOWER_KIND))
                .set(EarthField.SOURCE, person.key())
                .set(EarthField.TARGET, thing.key()));
    }
}
