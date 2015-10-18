package com.queatz.snappy.api;

import com.queatz.snappy.backend.Datastore;
import com.queatz.snappy.service.Api;
import com.queatz.snappy.service.Push;
import com.queatz.snappy.service.Thing;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.shared.PushSpec;
import com.queatz.snappy.shared.things.JoinLinkSpec;
import com.queatz.snappy.shared.things.PartySpec;

/**
 * Created by jacob on 2/8/15.
 */

public class Party extends Api.Path {
    public Party(Api api) {
        super(api);
    }

    @Override
    public void call() {
        String partyId;

        switch (method) {
            case GET:
                if (path.size() == 1) {
                    get(path.get(0));
                } else {
                    die("party - bad path");
                }

                break;
            case POST:
                if (path.size() != 1) {
                    die("party - bad path");
                }

                partyId = path.get(0);

                if (Boolean.valueOf(request.getParameter(Config.PARAM_JOIN))) {
                    postJoin(partyId);
                } else if (Boolean.valueOf(request.getParameter(Config.PARAM_CANCEL_JOIN))) {
                    postCancelJoin(partyId);
                } else if (Boolean.valueOf(request.getParameter(Config.PARAM_FULL))) {
                    postFull(partyId);
                } else {
                    die("party - bad path");
                }

                break;
            default:
                die("party - bad method");
        }
    }

    private void get(String partyId) {
        ok(Datastore.get(PartySpec.class, partyId));
    }

    private void postJoin(String partyId) {
        PartySpec party = Datastore.get(PartySpec.class, partyId);

        if (party != null) {
            String localId = request.getParameter(Config.PARAM_LOCAL_ID);
            JoinLinkSpec join = Thing.getService().join.create(user, partyId);

            if (join != null) {
                join.localId = localId;

                Push.getService().send(Datastore.id(party.hostId), new PushSpec(Config.PUSH_ACTION_JOIN_REQUEST, join));

                ok(join);
            }
        }
    }

    private void postCancelJoin(String partyId) {
        ok(Thing.getService().join.delete(user, partyId));
    }

    private void postFull(String partyId) {
        PartySpec party = Datastore.get(PartySpec.class, partyId);

        if (party == null || user == null || !user.id.equals(Datastore.id(party.hostId))) {
            ok(false);
        }

        Thing.getService().party.setFull(party);

        ok(true);
    }
}