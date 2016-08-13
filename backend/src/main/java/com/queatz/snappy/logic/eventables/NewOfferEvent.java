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
public class NewOfferEvent implements Eventable {

    EarthStore earthStore = new EarthStore(new EarthAs());

    Entity offer;

    // Serialization

    public NewOfferEvent() {}

    public NewOfferEvent fromData(String data) {
        offer = earthStore.get(data);
        return this;
    }

    public String toData() {
        return offer.key().name();
    }

    // End Serialization

    public NewOfferEvent(Entity offer) {
        this.offer = offer;
    }

    @Override
    public Object makePush() {
        return new PushSpec<>(
                Config.PUSH_ACTION_NEW_OFFER,
                ImmutableMap.of(
                        "id", offer.key().name(),
                        "details", offer.getString(EarthField.ABOUT),
                        "person", offer.getKey(EarthField.SOURCE).name() // go deeper {name: ...}
                )
        );
    }

    @Override
    public String makeSubject() {
        Entity person = earthStore.get(offer.getKey(EarthField.SOURCE));

        return person.getString(EarthField.FIRST_NAME) + " " + person.getString(EarthField.LAST_NAME) + " added a new offer";
    }

    @Override
    public String makeEmail() {
        Entity person = earthStore.get(offer.getKey(EarthField.SOURCE));
        String personUrl = Config.VILLAGE_WEBSITE + person.getString(EarthField.GOOGLE_URL);

        return offer.getString(EarthField.ABOUT) + "<br /><br /><span style=\"color: #757575;\">View their profile at " + personUrl + "</span>";
    }

    @Override
    public int emailDelay() {
        return 120;
    }
}
