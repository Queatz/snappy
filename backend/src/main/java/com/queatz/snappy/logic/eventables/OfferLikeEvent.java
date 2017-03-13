package com.queatz.snappy.logic.eventables;

import com.google.common.collect.ImmutableMap;
import com.queatz.snappy.backend.PushSpec;
import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.snappy.logic.EarthThing;
import com.queatz.snappy.logic.concepts.Eventable;
import com.queatz.snappy.shared.Config;

/**
 * Created by jacob on 6/19/16.
 */
public class OfferLikeEvent implements Eventable {

    EarthStore earthStore = new EarthStore(new EarthAs());

    EarthThing like;

    // Serialization

    public OfferLikeEvent() {}

    public OfferLikeEvent fromData(String data) {
        like = earthStore.get(data);
        return this;
    }

    public String toData() {
        return like.key().name();
    }

    // End Serialization

    public OfferLikeEvent(EarthThing like) {
        this.like = like;
    }

    @Override
    public Object makePush() {
        EarthThing source = earthStore.get(like.getKey(EarthField.SOURCE));
        EarthThing target = earthStore.get(like.getKey(EarthField.TARGET));

        return new PushSpec(
                Config.PUSH_ACTION_OFFER_LIKED,
                ImmutableMap.of(
                        "id", like.key().name(),
                        "source", ImmutableMap.of(
                                "id", source.key().name(),
                                "firstName", source.getString(EarthField.FIRST_NAME)
                        ),
                        "target", ImmutableMap.of(
                                "id", target.key().name(),
                                "name", target.getString(EarthField.ABOUT),
                                "want", target.has(EarthField.WANT) && target.getBoolean(EarthField.WANT)
                        )
                )
        );
    }

    @Override
    public String makeSubject() {
        EarthThing person = earthStore.get(like.getKey(EarthField.SOURCE));
        EarthThing offer = earthStore.get(like.getKey(EarthField.TARGET));

        boolean want = offer.has(EarthField.WANT) && offer.getBoolean(EarthField.WANT);

        return person.getString(EarthField.FIRST_NAME) + " " + person.getString(EarthField.LAST_NAME) + " liked your " +
                (want ? "want" : "offer");
    }

    @Override
    public String makeEmail() {
        EarthThing person = earthStore.get(like.getKey(EarthField.SOURCE));
        EarthThing offer = earthStore.get(like.getKey(EarthField.TARGET));

        return offer.getString(EarthField.NAME) +
                "<span style=\"color: #757575;\">View their profile at " +
                Config.VILLAGE_WEBSITE + person.getString(EarthField.GOOGLE_URL) + "</span>";
    }

    @Override
    public int emailDelay() {
        return 120;
    }
}
