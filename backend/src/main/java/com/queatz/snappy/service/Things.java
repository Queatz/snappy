package com.queatz.snappy.service;

import com.queatz.snappy.SnappyServlet;
import com.queatz.snappy.thing.Contact;
import com.queatz.snappy.thing.Follow;
import com.queatz.snappy.thing.Join;
import com.queatz.snappy.thing.Location;
import com.queatz.snappy.thing.Message;
import com.queatz.snappy.thing.Party;
import com.queatz.snappy.thing.Person;
import com.queatz.snappy.thing.Update;

/**
 * Created by jacob on 2/15/15.
 */
public class Things {
    public SnappyServlet snappy;
    public Party party;
    public Location location;
    public Message message;
    public Person person;
    public Update update;
    public Join join;
    public Follow follow;
    public Contact contact;

    public Things(SnappyServlet s) {
        snappy = s;
        party = new Party(this);
        location = new Location(this);
        message = new Message(this);
        person = new Person(this);
        join = new Join(this);
        follow = new Follow(this);
        contact = new Contact(this);
    }
}