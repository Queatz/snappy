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
public class FollowEvent implements Eventable {
    EarthStore earthStore = EarthSingleton.of(EarthStore.class);

    Entity follow;

    // Serialization

    public FollowEvent() {}

    public FollowEvent fromData(String data) {
        follow = earthStore.get(data);
        return this;
    }

    public String toData() {
        return follow.key().name();
    }

    // End Serialization

    public FollowEvent(Entity follow) {
        this.follow = follow;
    }

    @Override
    public Object makePush() {
        return new PushSpec<>(
                Config.PUSH_ACTION_CLEAR_NOTIFICATION,
                ImmutableMap.of(
                        "id", follow.key().name(),
                        "source", follow.getKey(EarthField.SOURCE)
                )
        );
    }

    @Override
    public String makeSubject() {
        Entity person = earthStore.get(follow.getKey(EarthField.SOURCE));

        return person.getString(EarthField.FIRST_NAME) + " started following you";// XXX TODO or your project?
    }

    @Override
    public String makeEmail() {
        Entity person = earthStore.get(follow.getKey(EarthField.SOURCE));
        String personUrl = Config.VILLAGE_WEBSITE + person.getString(EarthField.GOOGLE_URL);

        return "View their profile at " + personUrl;
    }

    @Override
    public int emailDelay() {
        return 120;
    }
}
