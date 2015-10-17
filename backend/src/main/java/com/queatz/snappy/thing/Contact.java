package com.queatz.snappy.thing;

import com.queatz.snappy.backend.Datastore;
import com.queatz.snappy.shared.things.ContactSpec;
import com.queatz.snappy.shared.things.MessageSpec;
import com.queatz.snappy.shared.things.PersonSpec;

import java.util.Date;

/**
 * Created by jacob on 2/21/15.
 */
public class Contact {
    public ContactSpec get(String personId, String contactId) {
        return Datastore.get(ContactSpec.class).filter("personId", personId).filter("contactId", contactId).first().now();
    }

    public boolean markSeen(PersonSpec user, String personId) {
        ContactSpec contact = get(user.id, personId);

        if(contact == null) {
            return false;
        }

        contact.seen = true;
        return Datastore.save(contact);
    }

    public void updateWithMessage(MessageSpec message) {
        for(String fromTo[] : new String[][] {
                new String [] {
                        Datastore.id(message.fromId),
                        Datastore.id(message.toId)
                },
                new String [] {
                        Datastore.id(message.toId),
                        Datastore.id(message.fromId)
                },
        }) {
            ContactSpec contact = get(fromTo[0], fromTo[1]);

            if(contact == null) {
                contact = new ContactSpec();
                contact.personId = Datastore.key(PersonSpec.class, fromTo[0]);
                contact.contactId = Datastore.key(PersonSpec.class, fromTo[1]);
            }

            contact.lastId = Datastore.key(message);
            contact.updated = new Date();
            contact.seen = false;

            Datastore.save(contact);
        }
    }
}