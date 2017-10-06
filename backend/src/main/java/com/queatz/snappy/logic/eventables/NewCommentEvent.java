package com.queatz.snappy.logic.eventables;

import com.google.common.collect.ImmutableMap;
import com.queatz.snappy.notifications.PushSpec;
import com.queatz.snappy.api.EarthAs;
import com.queatz.earth.EarthField;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.logic.concepts.Eventable;
import com.queatz.snappy.shared.Config;

/**
 * Created by jacob on 10/16/16.
 */
public class NewCommentEvent implements Eventable {

    EarthStore earthStore = new EarthStore(new EarthAs());

    EarthThing update;

    // Serialization

    public NewCommentEvent() {}

    public NewCommentEvent fromData(String data) {
        update = earthStore.get(data);
        return this;
    }

    public String toData() {
        return update.key().name();
    }

    // End Serialization

    public NewCommentEvent(EarthThing update) {
        this.update = update;
    }

    @Override
    public Object makePush() {
        EarthThing person = earthStore.get(update.getKey(EarthField.SOURCE));

        return new PushSpec(
                Config.PUSH_ACTION_NEW_COMMENT,
                ImmutableMap.of(
                        "id", update.key().name(),
                        "person", ImmutableMap.of(
                                "id", person.key().name(),
                                "firstName", person.getString(EarthField.FIRST_NAME)
                        )
                )
        );
    }

    @Override
    public String makeSubject() {
        EarthThing person = earthStore.get(update.getKey(EarthField.SOURCE));

        String subject;
        String name = person.getString(EarthField.FIRST_NAME) + " " + person.getString(EarthField.LAST_NAME);

        subject = name + " commented on your update";
        return subject;
    }

    @Override
    public String makeEmail() {
        EarthThing person = earthStore.get(update.getKey(EarthField.SOURCE));
        EarthThing updatedThing = earthStore.get(update.getKey(EarthField.TARGET));

        String personUrl = Config.VILLAGE_WEBSITE + person.getString(EarthField.GOOGLE_URL);
        String updateUrl = Config.VILLAGE_WEBSITE + updatedThing.getString(EarthField.KIND) + "s/" + updatedThing.key().name();

        String body;

        if (person.key().equals(updatedThing.key())) {
            body = "View their profile at " + personUrl + "<br /><br />";
        } else {
            body = "View " + updatedThing.getString(EarthField.NAME) + " at " + updateUrl
                    + "<br /><br />View their profile at " + personUrl;
        }

        return update.getString(EarthField.ABOUT) + "<br /><br /><span style=\"color: #757575;\">" + body + "</span>";
    }

    @Override
    public int emailDelay() {
        return 0;
    }
}
