package com.village.things;

import com.google.common.collect.ImmutableMap;
import com.google.common.html.HtmlEscapers;
import com.queatz.snappy.notifications.PushSpec;
import com.queatz.snappy.as.EarthAs;
import com.queatz.earth.EarthField;
import com.queatz.earth.EarthStore;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.events.Eventable;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.shared.Shared;

/**
 * Created by jacob on 6/19/16.
 */
public class MessageEvent implements Eventable {
    EarthStore earthStore = new EarthStore(new EarthAs());

    EarthThing message;

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

    public MessageEvent(EarthThing message) {
        this.message = message;
    }

    @Override
    public Object makePush() {
        EarthThing person = earthStore.get(message.getKey(EarthField.SOURCE));

        return new PushSpec(
                Config.PUSH_ACTION_MESSAGE,
                ImmutableMap.of(
                        "id", message.key().name(),
                        "from", ImmutableMap.of(
                                "id", person.key().name(),
                                "firstName", person.getString(EarthField.FIRST_NAME)
                        ),
                        "message", Shared.clip(message.getString(EarthField.MESSAGE)),
                        "photo", message.getBoolean(EarthField.PHOTO)
                )
        );
    }

    @Override
    public String makeSubject() {
        EarthThing person = earthStore.get(message.getKey(EarthField.SOURCE));

        return person.getString(EarthField.FIRST_NAME) + " " + person.getString(EarthField.LAST_NAME)  + " sent you a " +
                (message.getBoolean(EarthField.PHOTO) ? "photo" : "message");
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
