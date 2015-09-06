package com.queatz.snappy.things;

import java.util.Date;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by jacob on 8/29/15.
 */
public class Bounty extends RealmObject {
    private String id;
    private String details;
    private int price;
    private Person poster;
    private Date posted;
    private RealmList<Person> people;
    private String status;

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

    public Person getPoster() {
        return poster;
    }

    public void setPoster(Person poster) {
        this.poster = poster;
    }

    public Date getPosted() {
        return posted;
    }

    public void setPosted(Date posted) {
        this.posted = posted;
    }

    public RealmList<Person> getPeople() {
        return people;
    }

    public void setPeople(RealmList<Person> people) {
        this.people = people;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
