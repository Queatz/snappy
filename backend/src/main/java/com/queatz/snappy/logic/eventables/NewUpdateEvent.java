package com.queatz.snappy.logic.eventables;

import com.google.cloud.datastore.Entity;
import com.google.common.collect.ImmutableMap;
import com.queatz.snappy.backend.Util;
import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthKind;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.snappy.logic.concepts.Eventable;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.backend.PushSpec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jacob on 6/19/16.
 */
public class NewUpdateEvent implements Eventable {

    EarthStore earthStore = new EarthStore(new EarthAs());

    Entity update;

    // Serialization

    public NewUpdateEvent() {}

    public NewUpdateEvent fromData(String data) {
        update = earthStore.get(data);
        return this;
    }

    public String toData() {
        return update.key().name();
    }

    // End Serialization

    public NewUpdateEvent(Entity update) {
        this.update = update;
    }

    private List<Map<String, String>> getWith(Entity thing) {
        List<Entity> joins = earthStore.find(EarthKind.JOIN_KIND, EarthField.TARGET, thing.key());

        List<Map<String, String>> results = new ArrayList<>();

        Map<String, List<Map<String, String>>> resultsByKind = new HashMap<>();

        if (joins.isEmpty()) {
            return results;
        }

        for(Entity join : joins) {
            Entity with = earthStore.get(join.getKey(EarthField.SOURCE));
            String kind = with.getString(EarthField.KIND);
            String name;

            if (EarthKind.PERSON_KIND.equals(kind)) {
                name = with.getString(EarthField.FIRST_NAME) + " " + with.getString(EarthField.LAST_NAME);
            } else if (EarthKind.HUB_KIND.equals(kind)) {
                name = with.getString(EarthField.NAME);
            } else {
                continue;
            }

            if (!resultsByKind.containsKey(kind)) {
                resultsByKind.put(kind, new ArrayList<Map<String, String>>());
            }

            resultsByKind.get(kind).add(ImmutableMap.of(
                    "kind", kind,
                    "name", name
            ));
        }

        for (Map.Entry<String, List<Map<String, String>>> entry : resultsByKind.entrySet()) {
            int size = entry.getValue().size();

            if (size == 1) {
                results.add(entry.getValue().get(0));
            } else if (size > 1) {
                results.add(ImmutableMap.of(
                        "kind", entry.getKey(),
                        "name", size + " others"
                ));
            }
        }

        return results;
    }

    @Override
    public Object makePush() {
        Entity person = earthStore.get(update.getKey(EarthField.SOURCE));

        return new PushSpec(
                Config.PUSH_ACTION_NEW_UPTO,
                ImmutableMap.of(
                        "id", update.key().name(),
                        "photo", update.getBoolean(EarthField.PHOTO),
                        "person", ImmutableMap.of(
                                "id", person.key().name(),
                                "firstName", person.getString(EarthField.FIRST_NAME)
                        ),
                        "with", getWith(update)
                )
        );
    }

    @Override
    public String makeSubject() {
        Entity person = earthStore.get(update.getKey(EarthField.SOURCE));
        Entity updatedThing = earthStore.get(update.getKey(EarthField.TARGET));

        String subject;
        String name = person.getString(EarthField.FIRST_NAME) + " " + person.getString(EarthField.LAST_NAME);

        if (person.key().equals(updatedThing.key())) {
            subject = name + " posted a new update";
        } else {
            subject = name + " posted in " + updatedThing.getString(EarthField.NAME);
        }

        return subject;
    }

    @Override
    public String makeEmail() {
        Entity person = earthStore.get(update.getKey(EarthField.SOURCE));
        Entity updatedThing = earthStore.get(update.getKey(EarthField.TARGET));

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
        return 120;
    }
}
