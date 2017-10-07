package com.village.things;

import com.google.common.collect.ImmutableMap;
import com.queatz.earth.EarthField;
import com.queatz.earth.EarthStore;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.as.EarthAs;
import com.queatz.snappy.events.Eventable;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.shared.PushSpec;

/**
 * Created by jacob on 6/19/16.
 */
public class LikeEvent implements Eventable {

    EarthStore earthStore = new EarthStore(new EarthAs());

    EarthThing like;

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

    public LikeEvent(EarthThing like) {
        this.like = like;
    }

    @Override
    public Object makePush() {
        EarthThing person = earthStore.get(like.getKey(EarthField.SOURCE));
        EarthThing update = earthStore.get(like.getKey(EarthField.TARGET));

        return new PushSpec(
                Config.PUSH_ACTION_LIKE_UPDATE,
                ImmutableMap.of(
                        "id", like.key().name(),
                        "kind", update.getString(EarthField.KIND),
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
        EarthThing person = earthStore.get(like.getKey(EarthField.SOURCE));
        EarthThing update = earthStore.get(like.getKey(EarthField.TARGET));

        return person.getString(EarthField.FIRST_NAME) + " " + person.getString(EarthField.LAST_NAME) + " liked your " + (
                update.getBoolean(EarthField.PHOTO) ? "photo" : "update");
    }

    @Override
    public String makeEmail() {
        EarthThing person = earthStore.get(like.getKey(EarthField.SOURCE));

        return "<span style=\"color: #757575;\">View their profile at " + Config.VILLAGE_WEBSITE + person.getString(EarthField.GOOGLE_URL) + "</span>";
    }

    @Override
    public int emailDelay() {
        return 120;
    }
}
