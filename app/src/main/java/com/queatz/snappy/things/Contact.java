package com.queatz.snappy.things;

import com.queatz.snappy.things.Message;
import com.queatz.snappy.things.Person;
import com.queatz.snappy.things.Thing;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.Index;

/**
 * Created by jacob on 2/21/15.
 */
public class Contact extends RealmObject implements Thing {
    @Index
    private String id;
    private Person person;
    private Person contact;
    private Message last;
    private Date updated;
    private boolean unread;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public Person getContact() {
        return contact;
    }

    public void setContact(Person contact) {
        this.contact = contact;
    }

    public Message getLast() {
        return last;
    }

    public void setLast(Message last) {
        this.last = last;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    public boolean isUnread() {
        return unread;
    }

    public void setUnread(boolean unread) {
        this.unread = unread;
    }
}
