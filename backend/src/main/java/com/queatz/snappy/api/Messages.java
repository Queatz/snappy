package com.queatz.snappy.api;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.repackaged.com.google.common.collect.ImmutableMap;
import com.queatz.snappy.backend.Datastore;
import com.queatz.snappy.service.Api;
import com.queatz.snappy.shared.things.ContactSpec;
import com.queatz.snappy.shared.things.MessageSpec;
import com.queatz.snappy.shared.things.PersonSpec;
import com.queatz.snappy.thing.Person;

import java.io.IOException;

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
        ok(ImmutableMap.of("messages", Datastore.get(MessageSpec.class, Query.CompositeFilterOperator.or(
                new Query.FilterPredicate("fromId",
                        Query.FilterOperator.EQUAL,
                        Datastore.key(PersonSpec.class, user.id).getRaw()),
                new Query.FilterPredicate("toId",
                        Query.FilterOperator.EQUAL,
                        Datastore.key(PersonSpec.class, user.id).getRaw())
        )).list(), "contacts", Datastore.get(ContactSpec.class).filter("personId", user).list()));
    }

    private void get(String messageId) {
        MessageSpec message = Datastore.get(MessageSpec.class, messageId);

        if (!user.id.equals(Datastore.id(message.fromId)) && !user.id.equals(Datastore.id(message.toId))) {
            notFound();
        }

        ok(message);
    }
}

