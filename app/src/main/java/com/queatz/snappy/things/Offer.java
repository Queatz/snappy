package com.queatz.snappy.things;

import java.util.Date;

import io.realm.RealmObject;

/**
 * Created by jacob on 8/29/15.
 */
public class Offer extends RealmObject {
    private String id;
    private String details;
    private int price;
    private Person person;
    private Date created;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }
}
