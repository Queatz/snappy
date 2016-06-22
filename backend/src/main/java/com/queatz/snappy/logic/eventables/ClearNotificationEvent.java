package com.queatz.snappy.logic.eventables;

import com.google.cloud.datastore.Entity;
import com.queatz.snappy.logic.concepts.Eventable;

/**
 * Created by jacob on 6/19/16.
 */
public class ClearNotificationEvent implements Eventable {
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
        return 0;
    }
}
