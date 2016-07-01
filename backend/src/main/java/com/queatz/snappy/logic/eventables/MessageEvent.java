package com.queatz.snappy.logic.eventables;

import com.google.cloud.datastore.Entity;
import com.google.common.collect.ImmutableMap;
import com.google.common.html.HtmlEscapers;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthSingleton;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.snappy.logic.concepts.Eventable;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.shared.PushSpec;

/**
 * Created by jacob on 6/19/16.
 */
public class MessageEvent implements Eventable {
    EarthStore earthStore = EarthSingleton.of(EarthStore.class);

    Entity message;

    // Serialization

    public MessageEvent() {}

    public MessageEvent fromData(String data) {
        message = earthStore.get(data);
        return this;
    }

    public String toData() {
        return message.key().name();
    }

    // End Serialization

    public MessageEvent(Entity message) {
        this.message = message;
    }

    @Override
    public Object makePush() {
        return new PushSpec<>(
                Config.PUSH_ACTION_MESSAGE,
                ImmutableMap.of(
                        "id", message.key().name(),
                        "from", message.getKey(EarthField.SOURCE).name(), // go deeper {name: ...}
                        "message", message.getString(EarthField.MESSAGE)
                )
        );
    }

    @Override
    public String makeSubject() {
        Entity person = earthStore.get(message.getKey(EarthField.SOURCE));

        return person.getString(EarthField.FIRST_NAME) + " " + person.getString(EarthField.LAST_NAME)  + " sent you a message";
    }

    @Override
    public String makeEmail() {
        return HtmlEscapers.htmlEscaper().escape(message.getString(EarthField.MESSAGE)) +
                "<br /><br /><span style=\"color: #757575;\">View your message and reply at " + Config.VILLAGE_WEBSITE + "messages</span>";
    }

    @Override
    public int emailDelay() {
        return 0;
    }
}