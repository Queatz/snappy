package com.queatz.snappy.logic.views;

import com.google.api.client.util.Lists;
import com.google.cloud.datastore.Entity;
import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthControl;
import com.queatz.snappy.logic.EarthJson;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.snappy.logic.EarthView;
import com.queatz.snappy.logic.concepts.Viewable;

import java.util.List;

/**
 * Created by jacob on 5/14/16.
 */
public class MessagesAndContactsView extends EarthControl implements Viewable {

    final List<Viewable> messages;
    final List<Viewable> contacts;

    public MessagesAndContactsView(EarthAs as, List<Entity> messages, List<Entity> contacts) {
        super(as);

        this.messages = Lists.newArrayList();
        this.contacts = Lists.newArrayList();

        use(EarthStore.class).transact();

        for (Entity message : messages) {
            this.messages.add(new MessageView(as, message, EarthView.SHALLOW));
        }

        for (Entity contact : contacts) {
            this.contacts.add(new RecentView(as, contact, EarthView.SHALLOW));
        }

        use(EarthStore.class).commit();
    }

    @Override
    public String toJson() {
        return new EarthJson().toJson(this);
    }
}
