package com.queatz.snappy.thing;

import com.queatz.snappy.backend.Datastore;
import com.queatz.snappy.service.Thing;
import com.queatz.snappy.shared.things.MessageSpec;
import com.queatz.snappy.shared.things.PersonSpec;

import java.util.Date;

/**
 * Created by jacob on 2/15/15.
 */
public class Message {
    public MessageSpec newMessage(String from, String to, String text) {
        MessageSpec message = new MessageSpec();
        message.fromId = Datastore.key(PersonSpec.class, from);
        message.toId = Datastore.key(PersonSpec.class, to);
        message.message = text;
        message.date = new Date();

        Datastore.save(message);

        Thing.getService().contact.updateWithMessage(message);

        return message;
    }
}