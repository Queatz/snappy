package com.queatz.snappy.things;

import java.util.Date;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.RealmClass;

/**
 * Created by jacob on 2/14/15.
 */

@RealmClass
public class Party extends RealmObject {
    @Index
    private String id;
    private String name;
    private String details;
    private Date date;
    private Location location;
    private Person host;
    private boolean full;
    private RealmList<Update> updates;
    private RealmList<Join> people;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Person getHost() {
        return host;
    }

    public void setHost(Person host) {
        this.host = host;
    }

    public boolean isFull() {
        return full;
    }

    public void setFull(boolean full) {
        this.full = full;
    }

    public RealmList<Update> getUpdates() {
        return updates;
    }

    public void setUpdates(RealmList<Update> updates) {
        this.updates = updates;
    }

    public RealmList<Join> getPeople() {
        return people;
    }

    public void setPeople(RealmList<Join> people) {
        this.people = people;
    }
}
