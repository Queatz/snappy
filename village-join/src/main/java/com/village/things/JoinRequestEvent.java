package com.village.things;

import com.queatz.earth.EarthThing;
import com.queatz.snappy.shared.Config;

/**
 * Created by jacob on 8/23/16.
 */

public class JoinRequestEvent extends JoinEvent {
    public JoinRequestEvent() { super(); }

    public JoinRequestEvent(EarthThing join) {
        super(join);
    }

    @Override
    public Object makePush() {
        return makePush(Config.PUSH_ACTION_JOIN_REQUEST);
    }
}
