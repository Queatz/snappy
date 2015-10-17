package com.queatz.snappy.api;

import com.queatz.snappy.service.Api;
import com.queatz.snappy.service.Thing;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.shared.things.BountySpec;

/**
 * Created by jacob on 9/5/15.
 */
public class Bounties extends Api.Path {
    public Bounties(Api api) {
        super(api);
    }

    @Override
    public void call() {
        switch (method) {
            case POST:
                if (path.size() != 0) {
                    die("bounties - bad path");
                }

                post();

                break;
            default:
                die("bounties - bad method");
        }
    }

    private void post() {
        String localId = request.getParameter(Config.PARAM_LOCAL_ID);
        String details = request.getParameter(Config.PARAM_DETAILS);
        int price = 0;

        try {
            price = Integer.parseInt(request.getParameter(Config.PARAM_PRICE));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        BountySpec bounty = Thing.getService().bounty.create(user, details, price);
        bounty.localId = localId;

        ok(bounty);
    }
}

