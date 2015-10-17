package com.queatz.snappy.api;

import com.queatz.snappy.service.Api;
import com.queatz.snappy.service.Buy;
import com.queatz.snappy.service.Push;
import com.queatz.snappy.service.Thing;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.shared.PushSpec;
import com.queatz.snappy.shared.things.PartySpec;

import java.io.IOException;

/**
 * Created by jacob on 9/15/15.
 */
public class Parties extends Api.Path {
    public Parties(Api api) {
        super(api);
    }

    @Override
    public void call() throws IOException {
        switch (method) {
            case POST:
                if (path.size() != 0) {
                    die("parties - bad path");
                }

                post();

                break;
            default:
                die("parties - bad method");
        }
    }

    private void post() {
        if (!Buy.getService().valid(user)) {
            die("parties - not bought");
        }

        String localId = request.getParameter(Config.PARAM_LOCAL_ID);

        PartySpec party = Thing.getService().party.createFromRequest(request, user);

        if (party != null) {
            party.localId = localId;

            Push.getService().sendToFollowers(user.id, new PushSpec(Config.PUSH_ACTION_NEW_PARTY, party));

            ok(party);
        }
    }
}
