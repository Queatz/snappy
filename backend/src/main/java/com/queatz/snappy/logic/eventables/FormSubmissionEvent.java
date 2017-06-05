package com.queatz.snappy.logic.eventables;

import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.snappy.logic.EarthThing;
import com.queatz.snappy.logic.concepts.Eventable;
import com.queatz.snappy.shared.Config;

/**
 * Created by jacob on 6/4/17.
 */

public class FormSubmissionEvent implements Eventable {
    EarthStore earthStore = new EarthStore(new EarthAs());

    EarthThing thing;

    // Serialization

    public FormSubmissionEvent() {}

    public FormSubmissionEvent fromData(String data) {
        thing = earthStore.get(data);
        return this;
    }

    public String toData() {
        return thing.key().name();
    }

    // End Serialization

    public FormSubmissionEvent(EarthThing thing) {
        this.thing = thing;
    }

    @Override
    public Object makePush() {
        return null;
    }

    @Override
    public String makeSubject() {
        EarthThing person = earthStore.get(thing.getKey(EarthField.SOURCE));

        String name = person.getString(EarthField.FIRST_NAME) + " " + person.getString(EarthField.LAST_NAME);

        return name + " added a submission";
    }

    @Override
    public String makeEmail() {
        EarthThing form = earthStore.get(thing.getKey(EarthField.TARGET));
        EarthThing person = earthStore.get(thing.getKey(EarthField.SOURCE));

        String thingUrl = Config.VILLAGE_WEBSITE + thing.getString(EarthField.KIND) + "s/" + thing.key().name();

        return form.getString(EarthField.NAME) +
                "<br /><br /><span style=\"color: #757575;\">" +
                "View at " + thingUrl + "<br /><br />View their profile at " +
                Config.VILLAGE_WEBSITE +
                person.getString(EarthField.GOOGLE_URL) + "</span>";
    }
    @Override
    public int emailDelay() {
        return 0;
    }
}
