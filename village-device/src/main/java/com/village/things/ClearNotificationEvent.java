package com.village.things;

import com.google.common.collect.ImmutableMap;
import com.queatz.snappy.events.Eventable;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.notifications.PushSpec;

/**
 * Created by jacob on 6/19/16.
 */
public class ClearNotificationEvent implements Eventable {
    String notification;

    // Serialization

    public ClearNotificationEvent() {}

    public ClearNotificationEvent fromData(String data) {
        notification = data;
        return this;
    }

    public String toData() {
        return notification;
    }

    // End Serialization

    public ClearNotificationEvent(String notification) {
        this.notification = notification;
    }

    @Override
    public Object makePush() {
        return new PushSpec(
                Config.PUSH_ACTION_CLEAR_NOTIFICATION,
                ImmutableMap.of("notification", notification)
        );
    }

    @Override
    public String makeSubject() {
        return null;
    }

    @Override
    public String makeEmail() {
        return null;
    }

    @Override
    public int emailDelay() {
        return 0;
    }
}
