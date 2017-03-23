package com.queatz.snappy.logic.views;

import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthControl;
import com.queatz.snappy.logic.EarthJson;
import com.queatz.snappy.logic.EarthThing;
import com.queatz.snappy.logic.EarthView;
import com.queatz.snappy.logic.concepts.Viewable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jacob on 5/14/16.
 */
public class MessagesAndContactsView extends EarthControl implements Viewable {

    final List<Viewable> messages;
    final List<Viewable> contacts;

    public MessagesAndContactsView(EarthAs as, List<EarthThing> messages, List<EarthThing> contacts) {
        super(as);

        this.messages = new ArrayList<>();
        this.contacts = new ArrayList<>();

        for (EarthThing message : messages) {
            this.messages.add(new MessageView(as, message, EarthView.SHALLOW));
        }

        for (EarthThing contact : contacts) {
            this.contacts.add(new RecentView(as, contact, EarthView.SHALLOW));
        }
    }

    @Override
    public String toJson() {
        return new EarthJson().toJson(this);
    }
}
