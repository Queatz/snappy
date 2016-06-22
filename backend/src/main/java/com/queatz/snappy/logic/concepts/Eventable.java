package com.queatz.snappy.logic.concepts;

import com.google.cloud.datastore.Entity;

/**
 * Created by jacob on 6/19/16.
 */
public interface Eventable {
    String makePush(Entity thing);
    String makeSubject(Entity thing);
    String makeEmail(Entity thing);

    int emailDelay();
}
