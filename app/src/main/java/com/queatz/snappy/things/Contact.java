package com.queatz.snappy.things;

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
    private boolean seen;

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

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }
}
