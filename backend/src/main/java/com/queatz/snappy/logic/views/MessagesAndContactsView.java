package com.queatz.snappy.logic.views;

import com.google.api.client.util.Lists;
import com.google.cloud.datastore.Entity;
import com.queatz.snappy.logic.EarthJson;
import com.queatz.snappy.logic.EarthSingleton;
import com.queatz.snappy.logic.concepts.Viewable;

import java.util.List;

/**
 * Created by jacob on 5/14/16.
 */
public class MessagesAndContactsView implements Viewable {

    final List<Viewable> messages;
    final List<Viewable> contacts;

    public MessagesAndContactsView(List<Entity> messages, List<Entity> contacts) {
        this.messages = Lists.newArrayList();
        this.contacts = Lists.newArrayList();

        for (Entity message : messages) {
            this.messages.add(new MessageView(message));
        }

        for (Entity contact : contacts) {
            this.contacts.add(new ContactView(contact));
        }
    }

    @Override
    public String toJson() {
        return EarthSingleton.of(EarthJson.class).toJson(this);
    }
}
