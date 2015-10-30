package com.queatz.snappy.api;

import com.queatz.snappy.backend.Datastore;
import com.queatz.snappy.service.Api;
import com.queatz.snappy.service.Push;
import com.queatz.snappy.service.Thing;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.shared.PushSpec;
import com.queatz.snappy.shared.things.JoinLinkSpec;
import com.queatz.snappy.shared.things.PartySpec;

import java.io.IOException;

/**
 * Created by jacob on 2/18/15.
 */
public class Join extends Api.Path {
    public Join(Api api) {
        super(api);
    }

    @Override
    public void call() throws IOException {
        switch (method) {
            case GET:
                if (path.size() != 1) {
                    die("people - bad path");
                }

                get(path.get(0));

                break;
            case POST:
                if (path.size() != 1) {
                    die("join - bad path");
                }

                if (Boolean.valueOf(request.getParameter(Config.PARAM_HIDE))) {
                    postHide(path.get(0));
                } else if (Boolean.valueOf(request.getParameter(Config.PARAM_ACCEPT))) {
                    postAccept(path.get(0));
                } else {
                    die("join - bad path");
                }

                break;
            default:
                die("join - bad method");
        }
    }

    private void get(String joinId) {
        ok(Datastore.get(JoinLinkSpec.class, joinId));
    }

    private void postHide(String joinId) {
        boolean succeeded = false;
        JoinLinkSpec join = Datastore.get(JoinLinkSpec.class, joinId);

        if (join != null && Config.JOIN_STATUS_REQUESTED.equals(join.status)) {
            if (join.partyId != null) {
                PartySpec party = Datastore.get(join.partyId);

                if (user.id.equals(Datastore.id(party.hostId))) {
                    join = Thing.getService().join.setStatus(join, Config.JOIN_STATUS_OUT);
                    succeeded = true;
                }
            }
        }

        ok(succeeded);
    }

    private void postAccept(String joinId) {
        boolean succeeded = false;
        JoinLinkSpec join = Datastore.get(JoinLinkSpec.class, joinId);

        if (join != null && Config.JOIN_STATUS_REQUESTED.equals(join.status)) {
            PartySpec party = Datastore.get(join.partyId);

            if (party != null) {
                if (user.id.equals(Datastore.id(party.hostId))) {
                    join = Thing.getService().join.setStatus(join, Config.JOIN_STATUS_IN);
                    succeeded = true;
                }
            }
        }

        if (succeeded) {
            Push.getService().send(Datastore.id(join.personId), new PushSpec<>(Config.PUSH_ACTION_JOIN_ACCEPTED, join));
        }

        ok(succeeded);
    }
}
