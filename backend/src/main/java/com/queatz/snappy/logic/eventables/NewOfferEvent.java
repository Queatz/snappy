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
public class NewOfferEvent implements Eventable {
    EarthStore earthStore = EarthSingleton.of(EarthStore.class);

    @Override
    public String makePush(Entity thing) {
        return null;
    }

    @Override
    public String makeSubject(Entity thing) {
        Entity person = earthStore.get(thing.getKey(EarthField.SOURCE));

        return person.getString(EarthField.FIRST_NAME) + " " + person.getString(EarthField.LAST_NAME)  + " added a new offer";
    }

    @Override
    public String makeEmail(Entity thing) {
        Entity person = earthStore.get(thing.getKey(EarthField.SOURCE));
        String personUrl = Config.VILLAGE_WEBSITE + person.getString(EarthField.GOOGLE_URL);

        return thing.getString(EarthField.NAME) + "<br /><br />View their profile at " + personUrl;
    }

    @Override
    public int emailDelay() {
        return 120;
    }
}
