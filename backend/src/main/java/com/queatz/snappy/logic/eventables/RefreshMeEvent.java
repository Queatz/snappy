package com.queatz.snappy.logic.eventables;

import com.queatz.snappy.logic.concepts.Eventable;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.shared.PushSpec;

/**
 * Created by jacob on 6/19/16.
 */
public class RefreshMeEvent implements Eventable {

    // Serialization

    public RefreshMeEvent() {}

    public RefreshMeEvent fromData(String data) {
        return this;
    }

    public String toData() {
        return "";
    }

    // End Serialization
    @Override
    public Object makePush() {
        return new PushSpec<>(Config.PUSH_ACTION_OFFER_ENDORSEMENT);
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
