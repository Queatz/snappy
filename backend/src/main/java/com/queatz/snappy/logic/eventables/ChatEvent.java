package com.queatz.snappy.logic.eventables;

import com.google.common.collect.ImmutableMap;
import com.queatz.snappy.backend.PushSpec;
import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.snappy.logic.concepts.Eventable;
import com.queatz.snappy.shared.Config;

/**
 * Created by jacob on 9/18/17.
 */

public class ChatEvent implements Eventable {
    EarthStore earthStore = new EarthStore(new EarthAs());

    String topic;

    // Serialization

    public ChatEvent() {}

    public ChatEvent fromData(String data) {
        topic = data;
        return this;
    }

    public String toData() {
        return topic;
    }

    // End Serialization

    public ChatEvent(String topic) {
        this.topic = topic;
    }

    @Override
    public Object makePush() {
        return new PushSpec(
                Config.PUSH_ACTION_NEW_CHAT,
                ImmutableMap.of(
                        "topic", topic
                )
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
        return 120;
    }
}
