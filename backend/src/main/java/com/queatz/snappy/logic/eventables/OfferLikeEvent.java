package com.queatz.snappy.logic.eventables;

import com.google.cloud.datastore.Entity;
import com.google.common.collect.ImmutableMap;
import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.snappy.logic.concepts.Eventable;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.shared.PushSpec;

/**
 * Created by jacob on 6/19/16.
 */
public class OfferLikeEvent implements Eventable {

    EarthStore earthStore = new EarthStore(new EarthAs());

    Entity like;

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

    public OfferLikeEvent(Entity like) {
        this.like = like;
    }

    @Override
    public Object makePush() {
        return new PushSpec(
                Config.PUSH_ACTION_OFFER_LIKED,
                ImmutableMap.of(
                        "id", like.key().name(),
                        "source", like.getKey(EarthField.SOURCE).name(), // go deeper {name: ...}
                        "target", like.getKey(EarthField.TARGET).name() // go deeper {name: ...}
                )
        );
    }

    @Override
    public String makeSubject() {
        Entity person = earthStore.get(like.getKey(EarthField.SOURCE));
        Entity offer = earthStore.get(like.getKey(EarthField.TARGET));

        return person.getString(EarthField.FIRST_NAME) + " liked you for " + offer.getString(EarthField.NAME);
    }

    @Override
    public String makeEmail() {
        Entity person = earthStore.get(like.getKey(EarthField.SOURCE));

        return "<span style=\"color: #757575;\">View their profile at " + Config.VILLAGE_WEBSITE + person.getString(EarthField.GOOGLE_URL) + "</span>";
    }

    @Override
    public int emailDelay() {
        return 120;
    }
}
