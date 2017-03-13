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
public class NewPartyEvent implements Eventable {

    EarthStore earthStore = new EarthStore(new EarthAs());

    EarthThing party;

    // Serialization

    public NewPartyEvent() {}

    public NewPartyEvent fromData(String data) {
        party = earthStore.get(data);
        return this;
    }

    public String toData() {
        return party.key().name();
    }

    // End Serialization

    public NewPartyEvent(EarthThing party) {
        this.party = party;
    }

    @Override
    public Object makePush() {
        EarthThing host = earthStore.get(party.getKey(EarthField.HOST));

        return new PushSpec(
                Config.PUSH_ACTION_NEW_PARTY,
                ImmutableMap.of(
                        "id", party.key().name(),
                        "name", party.getString(EarthField.NAME),
                        "date", party.getDate(EarthField.DATE),
                        "host", ImmutableMap.of(
                                "id", host.key().name(),
                                "firstName", host.getString(EarthField.FIRST_NAME)
                        )
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
        return 120;
    }
}
