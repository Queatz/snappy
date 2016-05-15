package com.queatz.snappy.logic.editors;

import com.google.cloud.datastore.DateTime;
import com.google.cloud.datastore.Entity;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthKind;
import com.queatz.snappy.logic.EarthSingleton;
import com.queatz.snappy.logic.EarthStore;

import java.util.Date;

/**
 * Created by jacob on 5/8/16.
 */
public class RecentEditor {
    private final EarthStore earthStore = EarthSingleton.of(EarthStore.class);

    public Entity newRecent(Entity person, Entity with, Entity latest) {
        return earthStore.save(earthStore.edit(earthStore.create(EarthKind.RECENT_KIND))
                .set(EarthField.SOURCE, person)
                .set(EarthField.TARGET, with)
                .set(EarthField.UPDATED_ON, DateTime.now())
                .set(EarthField.SEEN, latest.getKey(EarthField.SOURCE).equals(person.key()))
                .set(EarthField.LATEST, latest.key()));
    }

    public void updateWithMessage(Entity message) {
//        for(Key<PersonSpec>[] fromTo : new Key[][] {
//                new Key[] {
//                        message.fromId,
//                        message.toId
//                },
//                new Key[] {
//                        message.toId,
//                        message.fromId
//                },
//        }) {
//            ContactSpec contact = get(fromTo[0], fromTo[1]);
//
//            if(contact == null) {
//                contact = Datastore.create(ContactSpec.class);
//                contact.personId = fromTo[0];
//                contact.contactId = fromTo[1];
//            }
//
//            contact.lastId = Datastore.key(message);
//            contact.updated = new Date();
//            contact.seen = message.fromId.equals(fromTo[0]);
//
//            Datastore.save(contact);
//        }
    }


    public Entity markSeen(Entity recent) {
        return earthStore.save(earthStore.edit(recent).set(EarthField.SEEN, true));
    }
}
