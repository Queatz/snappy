package com.queatz.snappy.logic.eventables;

import com.google.api.client.util.StringUtils;
import com.google.cloud.datastore.Entity;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthSingleton;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.snappy.logic.concepts.Eventable;
import com.queatz.snappy.shared.Config;

/**
 * Created by jacob on 7/5/16.
 */
public class NewContactEvent implements Eventable {

    EarthStore earthStore = EarthSingleton.of(EarthStore.class);

    Entity person;
    Entity contact;

    // Serialization

    public NewContactEvent() {}

    public NewContactEvent fromData(String data) {
        String[] string = data.split(",");
        person = earthStore.get(string[0]);
        contact = earthStore.get(string[1]);
        return this;
    }

    public String toData() {
        return person.key().name() + "," + contact.key().name();
    }

    // End Serialization

    public NewContactEvent(Entity person, Entity contact) {
        this.person = person;
        this.contact = contact;
    }

    @Override
    public Object makePush() {
        return null;
    }

    @Override
    public String makeSubject() {
        Entity thing = earthStore.get(contact.getKey(EarthField.SOURCE));

        String subject;
        String name = person.getString(EarthField.FIRST_NAME) + " " + person.getString(EarthField.FIRST_NAME);

        subject = name + " added you to " + thing.getString(EarthField.NAME);

        return subject;
    }

    @Override
    public String makeEmail() {
        Entity thing = earthStore.get(contact.getKey(EarthField.SOURCE));
        String thingUrl = Config.VILLAGE_WEBSITE + thing.getString(EarthField.KIND) + "s/" + thing.key().name();

        String body;

        body = "View " + thing.getString(EarthField.NAME) + " at " + thingUrl;

        return "You will now be visible to everybody.<br /><br /><span style=\"color: #757575;\">" + body + "</span>";
    }

    @Override
    public int emailDelay() {
        return 0;
    }
}
