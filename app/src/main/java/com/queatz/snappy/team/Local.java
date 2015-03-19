package com.queatz.snappy.team;

import android.support.annotation.NonNull;

import com.queatz.snappy.things.Contact;
import com.queatz.snappy.things.Message;

import io.realm.RealmResults;

/**
 * Created by jacob on 3/19/15.
 */
public class Local {
    public Team team;

    public Local(Team t) {
        team = t;
    }

    public void updateContactsForMessage(@NonNull Message message) {
        RealmResults<Contact> contacts = team.realm.where(Contact.class)
                .beginGroup()
                .equalTo("person.id", message.getFrom().getId())
                .equalTo("contact.id", message.getTo().getId())
                .endGroup()
                .or()
                .beginGroup()
                .equalTo("contact.id", message.getFrom().getId())
                .equalTo("person.id", message.getTo().getId())
                .endGroup()
                .findAll();

        team.realm.beginTransaction();

        for(int i = 0; i < contacts.size(); i++) {
            Contact contact = contacts.get(i);
            contact.setLast(message);

            if(!team.auth.getUser().equals(contact.getPerson().getId())) {
                contact.setSeen(false);
            }
        }

        team.realm.commitTransaction();
    }
}
