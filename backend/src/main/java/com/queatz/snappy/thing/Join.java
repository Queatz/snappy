package com.queatz.snappy.thing;

import com.queatz.snappy.backend.Datastore;
import com.queatz.snappy.service.Thing;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.shared.things.JoinLinkSpec;
import com.queatz.snappy.shared.things.PartySpec;
import com.queatz.snappy.shared.things.PersonSpec;

/**
 * Created by jacob on 2/16/15.
 */
public class Join {
    public JoinLinkSpec create(PersonSpec user, String partyId) {
        JoinLinkSpec join = Datastore.get(JoinLinkSpec.class).filter("personId", user.id).filter("partyId", partyId).first().now();

        if(join != null && !Config.JOIN_STATUS_WITHDRAWN.equals(join.status))
            return null;

        if(join == null) {
            join = new JoinLinkSpec();
            join.personId = Datastore.key(user);
            join.partyId = Datastore.key(PartySpec.class, partyId);
        }

        join.status = Config.JOIN_STATUS_REQUESTED;

        Datastore.save(join);
        return join;
    }

    public boolean delete(PersonSpec user, String partyId) {
        JoinLinkSpec join = Datastore.get(JoinLinkSpec.class).filter("personId", user.id).filter("partyId", partyId).first().now();

        if(join == null) {
            return false;
        }

        join.status = Config.JOIN_STATUS_WITHDRAWN;

        return Datastore.save(join);
    }

    public JoinLinkSpec setStatus(JoinLinkSpec join, String status) {
        join.status = status;
        Datastore.save(join);

        if(Config.JOIN_STATUS_IN.equals(status)) {
            Thing.getService().update.create(Config.UPDATE_ACTION_JOIN_PARTY, Datastore.get(join.personId), Datastore.get(join.partyId));
        }

        return join;
    }
}