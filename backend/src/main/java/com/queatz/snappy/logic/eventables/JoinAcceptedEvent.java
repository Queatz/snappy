package com.queatz.snappy.logic.eventables;

import com.google.cloud.datastore.Entity;
import com.queatz.snappy.shared.Config;

/**
 * Created by jacob on 8/23/16.
 */

public class JoinAcceptedEvent extends JoinEvent {
    public JoinAcceptedEvent() { super(); }

    public JoinAcceptedEvent(Entity join) {
        super(join);
    }

    @Override
    public Object makePush() {
        return makePush(Config.PUSH_ACTION_JOIN_ACCEPTED);
    }
}
