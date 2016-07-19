package com.queatz.snappy.logic.eventables;

import com.google.cloud.datastore.Entity;
import com.google.common.collect.ImmutableMap;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.snappy.logic.concepts.Eventable;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.shared.PushSpec;

/**
 * Created by jacob on 6/19/16.
 */
public class OfferEndorsementEvent implements Eventable {

    EarthStore earthStore = new EarthStore(null);

    Entity endorsement;

    // Serialization

    public OfferEndorsementEvent() {}

    public OfferEndorsementEvent fromData(String data) {
        endorsement = earthStore.get(data);
        return this;
    }

    public String toData() {
        return endorsement.key().name();
    }

    // End Serialization

    public OfferEndorsementEvent(Entity endorsement) {
        this.endorsement = endorsement;
    }

    @Override
    public Object makePush() {
        return new PushSpec<>(
                Config.PUSH_ACTION_OFFER_ENDORSEMENT,
                ImmutableMap.of(
                        "id", endorsement.key().name(),
                        "source", endorsement.getKey(EarthField.SOURCE).name(), // go deeper {name: ...}
                        "target", endorsement.getKey(EarthField.TARGET).name() // go deeper {name: ...}
                )
        );
    }

    @Override
    public String makeSubject() {
        Entity person = earthStore.get(endorsement.getKey(EarthField.SOURCE));
        Entity offer = earthStore.get(endorsement.getKey(EarthField.TARGET));

        return person.getString(EarthField.FIRST_NAME) + " endorsed you for " + offer.getString(EarthField.NAME);
    }

    @Override
    public String makeEmail() {
        Entity person = earthStore.get(endorsement.getKey(EarthField.SOURCE));

        return "<span style=\"color: #757575;\">View their profile at " + Config.VILLAGE_WEBSITE + person.getString(EarthField.GOOGLE_URL) + "</span>";
    }

    @Override
    public int emailDelay() {
        return 120;
    }
}
