package com.village.things;

import com.queatz.snappy.shared.PushSpec;
import com.queatz.snappy.events.Eventable;
import com.queatz.snappy.shared.Config;

/**
 * Created by jacob on 2/23/17.
 */

public class InformationEvent implements Eventable {

    // Serialization

    public InformationEvent() {}

    public InformationEvent fromData(String data) {
        return this;
    }

    public String toData() {
        return "";
    }

    // End Serialization
    @Override
    public Object makePush() {
        return new PushSpec(Config.PUSH_ACTION_INFORMATION);
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
