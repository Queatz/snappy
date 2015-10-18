package com.queatz.snappy.api;

import com.queatz.snappy.backend.Datastore;
import com.queatz.snappy.service.Api;
import com.queatz.snappy.service.Push;
import com.queatz.snappy.service.Thing;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.shared.things.BountySpec;

/**
 * Created by jacob on 9/5/15.
 */
public class Bounty extends Api.Path {
    public Bounty(Api api) {
        super(api);
    }

    @Override
    public void call() {
        switch (method) {
            case POST:
                if (path.size() != 1) {
                    die("bounty - bad path");
                }

                if (Boolean.valueOf(request.getParameter(Config.PARAM_CLAIM))) {
                    postClaim(path.get(0));
                } else if (Boolean.valueOf(request.getParameter(Config.PARAM_FINISH))) {
                    postFinish(path.get(0));
                }
                else {
                    die("bounty - bad path");
                }

                break;
            case DELETE:
                if (path.size() != 1) {
                    die("bounty - bad path");
                }

                delete(path.get(0));

                break;
            default:
                die("bounty - bad method");
        }
    }

    private void postClaim(String bountyId) {
        ok(Thing.getService().bounty.claim(user, bountyId));
    }

    private void postFinish(String bountyId) {
        BountySpec bounty = Datastore.get(BountySpec.class, bountyId);

        boolean finished = Thing.getService().bounty.finish(user, bountyId);

        if (finished) {
            Push.getService().send(Datastore.id(bounty.peopleId), bounty);
        }

        ok(finished);
    }

    private void delete(String bountyId) {
        BountySpec bounty = Datastore.get(BountySpec.class, bountyId);

        if (bounty != null && user.id.equals(Datastore.id(bounty.peopleId))) {
            Datastore.delete(bounty);
        }
    }
}
