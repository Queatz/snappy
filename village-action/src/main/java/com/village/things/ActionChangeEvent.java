package com.village.things;

import com.google.common.collect.ImmutableMap;
import com.queatz.earth.EarthField;
import com.queatz.earth.EarthStore;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.as.EarthAs;
import com.queatz.snappy.events.Eventable;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.shared.PushSpec;

/**
 * Created by jacob on 10/11/17.
 */

public class ActionChangeEvent implements Eventable {

    private EarthStore earthStore = new EarthAs().s(EarthStore.class);

    private EarthThing person;
    private EarthThing action;

    public ActionChangeEvent(EarthThing user, EarthThing action, String value) {}

    public ActionChangeEvent(EarthThing person, EarthThing action) {
        this.person = person;
        this.action = action;

    }

    @Override
    public Eventable fromData(String data) {
        String[] string = data.split(",");
        person = earthStore.get(string[0]);
        action = earthStore.get(string[1]);
        return this;
    }

    @Override
    public String toData() {
        return person.key().name() + "," + action.key().name();
    }

    @Override
    public Object makePush() {
        EarthThing target = earthStore.get(action.getKey(EarthField.TARGET));

        return new PushSpec(
                Config.PUSH_ACTION_ACTION_CHANGE,
                ImmutableMap.of(
                        "action", action.getString(EarthField.ROLE),
                        "value", action.getString(EarthField.MESSAGE),
                        "source", ImmutableMap.of(
                                "id", person.key().name(),
                                "firstName", person.getString(EarthField.FIRST_NAME)
                        ),
                        "target", ImmutableMap.of(
                                "id", target.key().name(),
                                "name", target.getString(EarthField.NAME)
                        )
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
        return 0;
    }
}
