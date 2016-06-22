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
public class MessageEvent implements Eventable {
    EarthStore earthStore = EarthSingleton.of(EarthStore.class);

    @Override
    public String makePush(Entity thing) {
        return null;
    }

    @Override
    public String makeSubject(Entity thing) {
        Entity person = earthStore.get(thing.getKey(EarthField.SOURCE));

        return person.getString(EarthField.FIRST_NAME) + " " + person.getString(EarthField.LAST_NAME)  + " sent you a message";
    }

    @Override
    public String makeEmail(Entity thing) {
        return thing.getString(EarthField.MESSAGE) + "<br /><br />View your message and reply at " + Config.VILLAGE_WEBSITE + "messages";
    }

    @Override
    public int emailDelay() {
        return 0;
    }
}
