package com.queatz.snappy.logic.eventables;

import com.google.cloud.datastore.Entity;
import com.google.common.collect.ImmutableMap;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthSingleton;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.snappy.logic.concepts.Eventable;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.shared.PushSpec;

/**
 * Created by jacob on 6/19/16.
 */
public class LikeEvent implements Eventable {

    EarthStore earthStore = EarthSingleton.of(EarthStore.class);

    Entity like;

    // Serialization

    public LikeEvent() {}

    public LikeEvent fromData(String data) {
        like = earthStore.get(data);
        return this;
    }

    public String toData() {
        return like.key().name();
    }

    // End Serialization

    public LikeEvent(Entity like) {
        this.like = like;
    }

    @Override
    public Object makePush() {
        return new PushSpec<>(
                Config.PUSH_ACTION_LIKE_UPDATE,
                ImmutableMap.of(
                        "id", like.key().name(),
                        "source", like.getKey(EarthField.SOURCE), // go deeper {name: ...}
                        "target", like.getKey(EarthField.TARGET) // go deeper {name: ...}
                )
        );
    }

    @Override
    public String makeSubject() {
        return null;
    }

    @Override
    public String makeEmail() {
        return null;
    }

    @Override
    public int emailDelay() {
        return 0;
    }
}
