package com.village.things;

import com.queatz.snappy.as.EarthAs;
import com.queatz.earth.EarthField;
import com.queatz.earth.EarthStore;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.events.EarthUpdate;
import com.queatz.snappy.view.EarthViewer;
import com.queatz.snappy.router.Interfaceable;
import com.queatz.snappy.exceptions.NothingLogicResponse;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.shared.Shared;

import java.util.Date;

/**
 * Created by jacob on 5/14/16.
 */
public class PartyInterface implements Interfaceable {

    @Override
    public String get(EarthAs as) {
        switch (as.getRoute().size()) {
            case 1:
                return new EarthViewer(as).getViewForEntityOrThrow(
                        new EarthStore(as).get(as.getRoute().get(0))).toJson();
        }

        throw new NothingLogicResponse("party - bad path");
    }

    @Override
    public String post(EarthAs as) {
        as.requireUser();

        EarthThing party;

        switch (as.getRoute().size()) {
            case 0:
                String localId = as.getRequest().getParameter(Config.PARAM_LOCAL_ID);
                String original = as.getRequest().getParameter(Config.PARAM_ID);
                String name = as.getRequest().getParameter(Config.PARAM_NAME);
                Date date = Shared.stringToDate(as.getRequest().getParameter(Config.PARAM_DATE));
                String locationParam = as.getRequest().getParameter(Config.PARAM_LOCATION);
                String details = as.getRequest().getParameter(Config.PARAM_DETAILS);

                party = new PartyEditor(as).newParty(
                        original,
                        as.getUser(),
                        name,
                        date,
                        locationParam,
                        details
                );

                new EarthUpdate(as)
                        .send(new NewPartyEvent(party))
                        .toFollowersOf(as.getUser());

                return new PartyView(as, party).setLocalId(localId).toJson();
            case 1:
                party = new EarthStore(as).get(as.getRoute().get(0));

                if (Boolean.valueOf(as.getRequest().getParameter(Config.PARAM_JOIN))) {
                    return postJoin(as, party);
                } else if (Boolean.valueOf(as.getRequest().getParameter(Config.PARAM_CANCEL_JOIN))) {
                    return postCancelJoin(as, party);
                } else if (Boolean.valueOf(as.getRequest().getParameter(Config.PARAM_FULL))) {
                    return new EarthViewer(as).getViewForEntityOrThrow(
                            new PartyEditor(as).setFull(party)).toJson();
                } else {
                    throw new NothingLogicResponse("party - bad path");
                }
        }

        throw new NothingLogicResponse("party - bad path");
    }

    private String postJoin(EarthAs as, EarthThing party) {
        as.requireUser();

        EarthThing join = new JoinEditor(as).newJoin(as.getUser(), party);
        String localId = as.getRequest().getParameter(Config.PARAM_LOCAL_ID);

        new EarthUpdate(as).send(new JoinRequestEvent(join))
                .to(party.getKey(EarthField.HOST));

        return new JoinView(as, join).setLocalId(localId).toJson();
    }

    private String postCancelJoin(EarthAs as, EarthThing party) {
        as.requireUser();

        EarthThing join = new JoinMine(as).byPersonAndParty(as.getUser(), party);

        new JoinEditor(as).setStatus(join, Config.JOIN_STATUS_WITHDRAWN);
        return null;
    }
}