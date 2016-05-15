package com.queatz.snappy.logic.editors;

import com.google.cloud.datastore.DateTime;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthKind;
import com.queatz.snappy.logic.EarthSingleton;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.snappy.logic.mines.RecentMine;

/**
 * Created by jacob on 5/8/16.
 */
public class RecentEditor {

    private final EarthStore earthStore = EarthSingleton.of(EarthStore.class);
    private final RecentMine recentMine = EarthSingleton.of(RecentMine.class);

    public Entity newRecent(Entity person, Entity with, Entity latest) {
        return earthStore.save(earthStore.edit(earthStore.create(EarthKind.RECENT_KIND))
                .set(EarthField.SOURCE, person.key())
                .set(EarthField.TARGET, with.key())
                .set(EarthField.UPDATED_ON, DateTime.now())
                .set(EarthField.SEEN, latest.getKey(EarthField.SOURCE).equals(person.key()))
                .set(EarthField.LATEST, latest.key()));
    }

    public void updateWithMessage(Entity message) {
        for(Key[] fromTo : new Key[][] {
                new Key[] {
                        message.getKey(EarthField.SOURCE),
                        message.getKey(EarthField.TARGET)
                },
                new Key[] {
                        message.getKey(EarthField.TARGET),
                        message.getKey(EarthField.SOURCE)
                },
        }) {
            Entity recent = recentMine.byPerson(fromTo[0], fromTo[1]);

            if(recent == null) {
                newRecent(earthStore.get(fromTo[0]), earthStore.get(fromTo[1]), message);
            } else {
                earthStore.save(earthStore.edit(message)
                        .set(EarthField.UPDATED_ON, DateTime.now())
                        .set(EarthField.LATEST, message.key())
                        .set(EarthField.SEEN, message.getKey(EarthField.SOURCE).equals(fromTo[0])));
            }
        }
    }

    public Entity markSeen(Entity recent) {
        return earthStore.save(earthStore.edit(recent).set(EarthField.SEEN, true));
    }
}
