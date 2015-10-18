package com.queatz.snappy.thing;

import com.queatz.snappy.backend.Datastore;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.shared.things.PartySpec;
import com.queatz.snappy.shared.things.PersonSpec;
import com.queatz.snappy.shared.things.UpdateSpec;

import java.util.Date;

/**
 * Created by jacob on 2/15/15.
 */
public class Update {
    public UpdateSpec create(String action, PersonSpec user, PartySpec party) {
        UpdateSpec update = Datastore.create(UpdateSpec.class);
        update.action = action;
        update.personId = Datastore.key(user);
        update.partyId = Datastore.key(party);
        update.date = new Date();
        Datastore.save(update);
        return update;
    }

    public UpdateSpec createUpto(String user) {
        UpdateSpec update = Datastore.create(UpdateSpec.class);
        update.action = Config.UPDATE_ACTION_UPTO;
        update.personId = Datastore.key(PersonSpec.class, user);
        update.date = new Date();
        Datastore.save(update);
        return update;
    }

    public UpdateSpec setMessage(UpdateSpec update, String message) {
        update.message = message == null ? "" : message;
        Datastore.save(update);
        return update;
    }
}