package com.queatz.snappy.api;

import com.google.appengine.api.datastore.Query;
import com.queatz.snappy.backend.Datastore;
import com.queatz.snappy.backend.Json;
import com.queatz.snappy.service.Api;
import com.queatz.snappy.shared.things.ContactSpec;
import com.queatz.snappy.shared.things.MessageSpec;

import java.io.IOException;
import java.util.List;

/**
 * Created by jacob on 2/14/15.
 */

public class Messages extends Api.Path {
    public Messages(Api api) {
        super(api);
    }

    @Override
    public void call() throws IOException {
        switch (method) {
            case GET:
                if (path.size() == 0) {
                    get();
                } else if (path.size() == 1) {
                    get(path.get(0));
                } else {
                    die("messages - bad path");
                }

                break;
            default:
               die("messages - bad method");
        }
    }

    private void get() {
        ok(new Object() {
            List<MessageSpec> messages = Datastore.get(MessageSpec.class, Query.CompositeFilterOperator.or(
                    new Query.FilterPredicate("from",
                            Query.FilterOperator.EQUAL,
                            user),
                    new Query.FilterPredicate("to",
                            Query.FilterOperator.EQUAL,
                            user)
            )).list();
            List<ContactSpec> contacts = Datastore.get(ContactSpec.class).filter("personId", user).list();
        }, Json.Compression.SHALLOW);
    }

    private void get(String messageId) {
        MessageSpec message = Datastore.get(MessageSpec.class, messageId);

        if (!user.equals(Datastore.id(message.fromId)) && !user.equals(Datastore.id(message.toId))) {
            notFound();
        }

        ok(message);
    }
}

