package com.queatz.snappy.logic.eventables;

import com.queatz.snappy.api.EarthAs;
import com.queatz.earth.EarthField;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.logic.concepts.Eventable;
import com.queatz.snappy.shared.Config;

/**
 * Created by jacob on 7/5/16.
 */
public class NewContactEvent implements Eventable {

    EarthStore earthStore = new EarthStore(new EarthAs());

    EarthThing person;
    EarthThing contact;

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

    public NewContactEvent(EarthThing person, EarthThing contact) {
        this.person = person;
        this.contact = contact;
    }

    @Override
    public Object makePush() {
        return null;
    }

    @Override
    public String makeSubject() {
        EarthThing thing = earthStore.get(contact.getKey(EarthField.SOURCE));

        String subject;
        String name = person.getString(EarthField.FIRST_NAME) + " " + person.getString(EarthField.LAST_NAME);

        subject = name + " added you as a contact for " + thing.getString(EarthField.NAME);

        return subject;
    }

    @Override
    public String makeEmail() {
        EarthThing thing = earthStore.get(contact.getKey(EarthField.SOURCE));
        String thingUrl = Config.VILLAGE_WEBSITE + thing.getString(EarthField.KIND) + "s/" + thing.key().name();

        String body;

        body = "View " + thing.getString(EarthField.NAME) + " at " + thingUrl;

        return "You may be contacted by people about this.<br /><br /><span style=\"color: #757575;\">" + body + "</span>";
    }

    @Override
    public int emailDelay() {
        return 0;
    }
}
