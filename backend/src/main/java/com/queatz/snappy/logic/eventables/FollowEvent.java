package com.queatz.snappy.logic.eventables;

import com.google.common.collect.ImmutableMap;
import com.queatz.snappy.notifications.PushSpec;
import com.queatz.snappy.api.EarthAs;
import com.queatz.earth.EarthField;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.logic.concepts.Eventable;
import com.queatz.snappy.shared.Config;

/**
 * Created by jacob on 6/19/16.
 */
public class FollowEvent implements Eventable {

    EarthStore earthStore = new EarthStore(new EarthAs());

    EarthThing follow;

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

    public FollowEvent(EarthThing follow) {
        this.follow = follow;
    }

    @Override
    public Object makePush() {
        EarthThing person = earthStore.get(follow.getKey(EarthField.SOURCE));

        return new PushSpec(
                Config.PUSH_ACTION_FOLLOW,
                ImmutableMap.of(
                        "id", follow.key().name(),
                        "source", ImmutableMap.of(
                                "id", person.key().name(),
                                "firstName", person.getString(EarthField.FIRST_NAME)
                        )
                )
        );
    }

    @Override
    public String makeSubject() {
        EarthThing person = earthStore.get(follow.getKey(EarthField.SOURCE));

        String name = person.getString(EarthField.FIRST_NAME) + " " + person.getString(EarthField.LAST_NAME);

        return name + " started backing you on Village"; // XXX TODO or your project?
    }

    @Override
    public String makeEmail() {
        EarthThing person = earthStore.get(follow.getKey(EarthField.SOURCE));
        String personUrl = Config.VILLAGE_WEBSITE + person.getString(EarthField.GOOGLE_URL);

        return "<span style=\"color: #757575;\">View their profile at " + personUrl + "</span>";
    }

    @Override
    public int emailDelay() {
        return 120;
    }
}
