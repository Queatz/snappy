package com.queatz.snappy.logic.eventables;

import com.google.cloud.datastore.Entity;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthSingleton;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.snappy.logic.concepts.Eventable;
import com.queatz.snappy.shared.Config;

/**
 * Created by jacob on 6/19/16.
 */
public class NewUpdateEvent implements Eventable {
    EarthStore earthStore = EarthSingleton.of(EarthStore.class);

    @Override
    public String makePush(Entity thing) {
        return null;
    }

    @Override
    public String makeSubject(Entity thing) {
        Entity person = earthStore.get(thing.getKey(EarthField.SOURCE));
        Entity updatedThing = earthStore.get(thing.getKey(EarthField.TARGET));

        return person.getString(EarthField.FIRST_NAME) + " posted in " + updatedThing.getString(EarthField.NAME);
    }

    @Override
    public String makeEmail(Entity thing) {
        Entity person = earthStore.get(thing.getKey(EarthField.SOURCE));
        Entity updatedThing = earthStore.get(thing.getKey(EarthField.TARGET));

        String personUrl = Config.VILLAGE_WEBSITE + person.getString(EarthField.GOOGLE_URL);
        String updateUrl = Config.VILLAGE_WEBSITE + updatedThing.key().name();

        return "View their profile at " + personUrl + "<br /><br />View " + updatedThing.getString(EarthField.NAME) + " at " + updateUrl;
    }

    @Override
    public int emailDelay() {
        return 120;
    }
}
