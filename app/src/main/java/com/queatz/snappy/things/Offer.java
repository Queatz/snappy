package com.queatz.snappy.things;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.RealmClass;

/**
 * Created by jacob on 8/29/15.
 */
@RealmClass
public class Offer extends RealmObject {
    private String id;
    private String details;
    private int price;
    private String unit;
    private Person person;
    private Date created;
    private int endorsers;

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

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
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

    public int getEndorsers() {
        return endorsers;
    }

    public void setEndorsers(int endorsers) {
        this.endorsers = endorsers;
    }
}
