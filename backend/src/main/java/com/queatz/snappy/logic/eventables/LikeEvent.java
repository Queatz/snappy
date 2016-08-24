package com.queatz.snappy.logic.eventables;

import com.google.cloud.datastore.Entity;
import com.google.common.collect.ImmutableMap;
import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.snappy.logic.concepts.Eventable;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.backend.PushSpec;

/**
 * Created by jacob on 6/19/16.
 */
public class LikeEvent implements Eventable {

    EarthStore earthStore = new EarthStore(new EarthAs());

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
        Entity person = earthStore.get(like.getKey(EarthField.SOURCE));
        Entity update = earthStore.get(like.getKey(EarthField.TARGET));

        return new PushSpec(
                Config.PUSH_ACTION_LIKE_UPDATE,
                ImmutableMap.of(
                        "id", like.key().name(),
                        "source", ImmutableMap.of(
                                "id", person.key().name(),
                                "firstName", person.getString(EarthField.FIRST_NAME)
                        ),
                        "photo", update.getBoolean(EarthField.PHOTO)
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
