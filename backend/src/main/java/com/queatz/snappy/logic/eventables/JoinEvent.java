package com.queatz.snappy.logic.eventables;

import com.google.common.collect.ImmutableMap;
import com.queatz.snappy.notifications.PushSpec;
import com.queatz.snappy.api.EarthAs;
import com.queatz.earth.EarthField;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.logic.concepts.Eventable;

/**
 * Created by jacob on 6/19/16.
 */
public abstract class JoinEvent implements Eventable {

    EarthStore earthStore = new EarthStore(new EarthAs());

    EarthThing join;

    // Serialization

    public JoinEvent() {}

    public JoinEvent fromData(String data) {
        join = earthStore.get(data);
        return this;
    }

    public String toData() {
        return join.key().name();
    }

    // End Serialization

    public JoinEvent(EarthThing join) {
        this.join = join;
    }

    public Object makePush(String action) {
        EarthThing person = earthStore.get(join.getKey(EarthField.SOURCE));
        EarthThing party = earthStore.get(join.getKey(EarthField.TARGET));

        return new PushSpec(
                action,
                ImmutableMap.of(
                        "id", join.key().name(),
                        "person", ImmutableMap.of(
                                "id", person.key().name(),
                                "firstName", person.getString(EarthField.FIRST_NAME)
                        ),
                        "party", ImmutableMap.of(
                                "id", party.key().name(),
                                "name", party.getString(EarthField.NAME),
                                "date", party.getDate(EarthField.DATE)
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
        return 0;
    }
}
