package com.queatz.snappy.logic.eventables;

import com.google.common.collect.ImmutableMap;
import com.queatz.snappy.backend.PushSpec;
import com.queatz.snappy.logic.EarthAs;
import com.queatz.earth.EarthField;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.logic.concepts.Eventable;
import com.queatz.snappy.shared.Config;

/**
 * Created by jacob on 6/19/16.
 */
public class NewOfferEvent implements Eventable {

    EarthStore earthStore = new EarthStore(new EarthAs());

    EarthThing offer;

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

    public NewOfferEvent(EarthThing offer) {
        this.offer = offer;
    }

    @Override
    public Object makePush() {
        EarthThing person = earthStore.get(offer.getKey(EarthField.SOURCE));

        return new PushSpec(
                Config.PUSH_ACTION_NEW_OFFER,
                ImmutableMap.of(
                        "id", offer.key().name(),
                        "details", offer.getString(EarthField.ABOUT),
                        "person", ImmutableMap.of(
                                "id", person.key().name(),
                                "firstName", person.getString(EarthField.FIRST_NAME)
                        )
                )
        );
    }

    @Override
    public String makeSubject() {
        EarthThing person = earthStore.get(offer.getKey(EarthField.SOURCE));

        boolean want = offer.has(EarthField.WANT) && offer.getBoolean(EarthField.WANT);

        return person.getString(EarthField.FIRST_NAME) + " " + person.getString(EarthField.LAST_NAME) + " added a new " +
                (want ? "request" : "offer");
    }

    @Override
    public String makeEmail() {
        EarthThing person = earthStore.get(offer.getKey(EarthField.SOURCE));
        String personUrl = Config.VILLAGE_WEBSITE + person.getString(EarthField.GOOGLE_URL);

        return offer.getString(EarthField.ABOUT) + "<br /><br /><span style=\"color: #757575;\">View their profile at " + personUrl + "</span>";
    }

    @Override
    public int emailDelay() {
        return 120;
    }
}
