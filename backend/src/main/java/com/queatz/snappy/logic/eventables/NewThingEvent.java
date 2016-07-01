package com.queatz.snappy.logic.eventables;

import com.google.cloud.datastore.Entity;
import com.google.common.collect.ImmutableMap;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthSingleton;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.snappy.logic.concepts.Eventable;
import com.queatz.snappy.logic.mines.ContactMine;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.shared.PushSpec;

/**
 * Created by jacob on 6/26/16.
 */
public class NewThingEvent implements Eventable {
    EarthStore earthStore = EarthSingleton.of(EarthStore.class);
    ContactMine contactMine = EarthSingleton.of(ContactMine.class);

    Entity thing;

    // Serialization

    public NewThingEvent() {}

    public NewThingEvent fromData(String data) {
        thing = earthStore.get(data);
        return this;
    }

    public String toData() {
        return thing.key().name();
    }

    // End Serialization

    public NewThingEvent(Entity thing) {
        this.thing = thing;
    }

    @Override
    public Object makePush() {
        return new PushSpec<>(
                Config.PUSH_ACTION_NEW_PARTY,
                ImmutableMap.of(
                        "id", thing.key().name(),
                        "name", thing.getString(EarthField.NAME)
                )
        );
    }

    @Override
    public String makeSubject() {
        Entity contact = contactMine.getContacts(thing).get(0);
        Entity person = earthStore.get(contact.getKey(EarthField.TARGET));

        return person.getString(EarthField.FIRST_NAME) + " add a new " + thing.getString(EarthField.KIND);
    }

    @Override
    public String makeEmail() {
        Entity contact = contactMine.getContacts(thing).get(0);
        Entity person = earthStore.get(contact.getKey(EarthField.TARGET));

        return thing.getString(EarthField.NAME) +
                "<br /><br /><span style=\"color: #757575;\">View their profile at " +
                Config.VILLAGE_WEBSITE +
                person.getString(EarthField.GOOGLE_URL) + "</span>";
    }
    @Override
    public int emailDelay() {
        return 120;
    }
}