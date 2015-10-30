package com.queatz.snappy.thing;

import com.googlecode.objectify.Key;
import com.queatz.snappy.backend.Datastore;
import com.queatz.snappy.shared.things.ContactSpec;
import com.queatz.snappy.shared.things.MessageSpec;
import com.queatz.snappy.shared.things.PersonSpec;

import java.util.Date;

/**
 * Created by jacob on 2/21/15.
 */
public class Contact {
    public ContactSpec get(Object personId, Object contactId) {
        return Datastore.get(ContactSpec.class).filter("personId", personId).filter("contactId", contactId).first().now();
    }

    public boolean markSeen(PersonSpec user, String personId) {
        ContactSpec contact = get(user, Datastore.key(PersonSpec.class, personId));

        if(contact == null) {
            return false;
        }

        contact.seen = true;
        return Datastore.save(contact);
    }

    public void updateWithMessage(MessageSpec message) {
        for(Key<PersonSpec>[] fromTo : new Key[][] {
                new Key[] {
                        message.fromId,
                        message.toId
                },
                new Key[] {
                        message.toId,
                        message.fromId
                },
        }) {
            ContactSpec contact = get(fromTo[0], fromTo[1]);

            if(contact == null) {
                contact = Datastore.create(ContactSpec.class);
                contact.personId = fromTo[0];
                contact.contactId = fromTo[1];
            }

            contact.lastId = Datastore.key(message);
            contact.updated = new Date();
            contact.seen = message.fromId.equals(fromTo[0]);

            Datastore.save(contact);
        }
    }
}