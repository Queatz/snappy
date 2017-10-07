package com.village.things;

import com.google.common.collect.ImmutableMap;
import com.queatz.earth.EarthField;
import com.queatz.earth.EarthStore;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.as.EarthAs;
import com.queatz.snappy.events.Eventable;
import com.queatz.snappy.plugins.ContactMinePlugin;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.shared.PushSpec;

/**
 * Created by jacob on 6/26/16.
 */
public class NewThingEvent implements Eventable {

    private EarthStore earthStore = new EarthStore(new EarthAs());
    private ContactMinePlugin contactMine = new EarthAs().s(ContactMinePlugin.class);

    private EarthThing thing;

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

    public NewThingEvent(EarthThing thing) {
        this.thing = thing;
    }

    @Override
    public Object makePush() {
        return new PushSpec(
                Config.PUSH_ACTION_NEW_THING,
                ImmutableMap.of(
                        "id", thing.key().name(),
                        "name", thing.getString(EarthField.NAME)
                )
        );
    }

    @Override
    public String makeSubject() {
        EarthThing contact = contactMine.getContacts(thing).get(0);
        EarthThing person = earthStore.get(contact.getKey(EarthField.TARGET));

        String name = person.getString(EarthField.FIRST_NAME) + " " + person.getString(EarthField.LAST_NAME);

        return name + " added a new " + thing.getString(EarthField.KIND);
    }

    @Override
    public String makeEmail() {
        EarthThing contact = contactMine.getContacts(thing).get(0);
        EarthThing person = earthStore.get(contact.getKey(EarthField.TARGET));

        String thingUrl = Config.VILLAGE_WEBSITE + thing.getString(EarthField.KIND) + "s/" + thing.key().name();

        return thing.getString(EarthField.NAME) +
                "<br /><br /><span style=\"color: #757575;\">" +
                "View at " + thingUrl + "<br /><br />View their profile at " +
                Config.VILLAGE_WEBSITE +
                person.getString(EarthField.GOOGLE_URL) + "</span>";
    }
    @Override
    public int emailDelay() {
        return 120;
    }
}
