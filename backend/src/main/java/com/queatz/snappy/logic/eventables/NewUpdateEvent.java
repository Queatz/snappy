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
public class NewUpdateEvent implements Eventable {
    EarthStore earthStore = EarthSingleton.of(EarthStore.class);

    Entity update;

    // Serialization

    public NewUpdateEvent() {}

    public NewUpdateEvent fromData(String data) {
        update = earthStore.get(data);
        return this;
    }

    public String toData() {
        return update.key().name();
    }

    // End Serialization

    public NewUpdateEvent(Entity update) {
        this.update = update;
    }

    @Override
    public Object makePush() {
        return new PushSpec<>(
                Config.PUSH_ACTION_NEW_UPTO,
                ImmutableMap.of(
                        "id", update.key().name(),
                        "person", update.getKey(EarthField.SOURCE).name(), // go deeper {name: ...}
                        "party", update.getKey(EarthField.TARGET).name() // go deeper {name: ...}, not party
                )
        );
    }

    @Override
    public String makeSubject() {
        Entity person = earthStore.get(update.getKey(EarthField.SOURCE));
        Entity updatedThing = earthStore.get(update.getKey(EarthField.TARGET));

        return person.getString(EarthField.FIRST_NAME) + " posted in " + updatedThing.getString(EarthField.NAME);
    }

    @Override
    public String makeEmail() {
        Entity person = earthStore.get(update.getKey(EarthField.SOURCE));
        Entity updatedThing = earthStore.get(update.getKey(EarthField.TARGET));

        String personUrl = Config.VILLAGE_WEBSITE + person.getString(EarthField.GOOGLE_URL);
        String updateUrl = Config.VILLAGE_WEBSITE + updatedThing.key().name();

        return "View their profile at " + personUrl + "<br /><br />View " + updatedThing.getString(EarthField.NAME) + " at " + updateUrl;
    }

    @Override
    public int emailDelay() {
        return 120;
    }
}
