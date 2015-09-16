package com.queatz.snappy.service;

import com.queatz.snappy.thing.Bounty;
import com.queatz.snappy.thing.Buy;
import com.queatz.snappy.thing.Contact;
import com.queatz.snappy.thing.Follow;
import com.queatz.snappy.thing.Join;
import com.queatz.snappy.thing.Location;
import com.queatz.snappy.thing.Message;
import com.queatz.snappy.thing.Offer;
import com.queatz.snappy.thing.Party;
import com.queatz.snappy.thing.Person;
import com.queatz.snappy.thing.Quest;
import com.queatz.snappy.thing.QuestPerson;
import com.queatz.snappy.thing.Update;

/**
 * Created by jacob on 2/15/15.
 */
public class Things {
    private static Things _service;

    public static Things getService() {
        if(_service == null)
            _service = new Things();

        return _service;
    }

    public Party party;
    public Location location;
    public Message message;
    public Person person;
    public Update update;
    public Join join;
    public Follow follow;
    public Contact contact;
    public Buy buy;
    public Offer offer;
    public Bounty bounty;
    public Quest quest;
    public QuestPerson questPerson;

    public Things() {
        party = new Party();
        location = new Location();
        message = new Message();
        person = new Person();
        join = new Join();
        follow = new Follow();
        contact = new Contact();
        buy = new Buy();
        update = new Update();
        offer = new Offer();
        bounty = new Bounty();
        quest = new Quest();
        questPerson = new QuestPerson();
    }
}