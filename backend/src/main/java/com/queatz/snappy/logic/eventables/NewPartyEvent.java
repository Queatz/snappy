package com.queatz.snappy.logic.eventables;

import com.google.cloud.datastore.Entity;
import com.queatz.snappy.logic.EarthSingleton;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.snappy.logic.concepts.Eventable;

/**
 * Created by jacob on 6/19/16.
 */
public class NewPartyEvent implements Eventable {
    EarthStore earthStore = EarthSingleton.of(EarthStore.class);

    @Override
    public String makePush(Entity thing) {
        return null;
    }

    @Override
    public String makeSubject(Entity thing) {
        return null;
    }

    @Override
    public String makeEmail(Entity thing) {
        return null;
    }

    @Override
    public int emailDelay() {
        return 120;
    }
}
