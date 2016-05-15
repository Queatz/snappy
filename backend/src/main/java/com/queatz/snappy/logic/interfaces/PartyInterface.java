package com.queatz.snappy.logic.interfaces;

import com.google.cloud.datastore.Entity;
import com.queatz.snappy.backend.Util;
import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthSingleton;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.snappy.logic.concepts.Interfaceable;
import com.queatz.snappy.logic.editors.JoinEditor;
import com.queatz.snappy.logic.editors.PartyEditor;
import com.queatz.snappy.logic.exceptions.NothingLogicResponse;
import com.queatz.snappy.logic.mines.JoinMine;
import com.queatz.snappy.logic.views.JoinView;
import com.queatz.snappy.logic.views.PartyView;
import com.queatz.snappy.service.Push;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.shared.PushSpec;

import java.util.Date;

/**
 * Created by jacob on 5/14/16.
 */
public class PartyInterface implements Interfaceable {

    EarthStore earthStore = EarthSingleton.of(EarthStore.class);
    PartyEditor partyEditor = EarthSingleton.of(PartyEditor.class);
    JoinMine joinMine = EarthSingleton.of(JoinMine.class);
    JoinEditor joinEditor = EarthSingleton.of(JoinEditor.class);

    @Override
    public String get(EarthAs as) {
        switch (as.getRoute().size()) {
            case 1:
                return new PartyView(earthStore.get(as.getRoute().get(0))).toJson();
        }

        throw new NothingLogicResponse("party - bad path");
    }

    @Override
    public String post(EarthAs as) {
        Entity party;

        switch (as.getRoute().size()) {
            case 0:
                String localId = as.getRequest().getParameter(Config.PARAM_LOCAL_ID);
                String original = as.getRequest().getParameter(Config.PARAM_ID);
                String name = as.getRequest().getParameter(Config.PARAM_NAME);
                Date date = Util.stringToDate(as.getRequest().getParameter(Config.PARAM_DATE));
                String locationParam = as.getRequest().getParameter(Config.PARAM_LOCATION);
                String details = as.getRequest().getParameter(Config.PARAM_DETAILS);

                party = partyEditor.newParty(
                        original,
                        as.getUser(),
                        name,
                        date,
                        locationParam,
                        details
                );

                Push.getService().sendToFollowers(as.getUser().key().name(), new PushSpec<>(Config.PUSH_ACTION_NEW_PARTY, party));

                return new PartyView(party).setLocalId(localId).toJson();
            case 1:
                party = earthStore.get(as.getRoute().get(0));

                if (Boolean.valueOf(as.getRequest().getParameter(Config.PARAM_JOIN))) {
                    return postJoin(as, party);
                } else if (Boolean.valueOf(as.getRequest().getParameter(Config.PARAM_CANCEL_JOIN))) {
                    return postCancelJoin(as, party);
                } else if (Boolean.valueOf(as.getRequest().getParameter(Config.PARAM_FULL))) {
                    return new PartyView(partyEditor.setFull(party)).toJson();
                } else {
                    throw new NothingLogicResponse("party - bad path");
                }
        }

        throw new NothingLogicResponse("party - bad path");
    }

    private String postJoin(EarthAs as, Entity party) {
        Entity join = joinEditor.newJoin(as.getUser(), party);
        String localId = as.getRequest().getParameter(Config.PARAM_LOCAL_ID);

        Push.getService().send(party.getKey(EarthField.HOST).name(), new PushSpec<>(Config.PUSH_ACTION_JOIN_REQUEST, join));

        return new JoinView(join).setLocalId(localId).toJson();
    }

    private String postCancelJoin(EarthAs as, Entity party) {
        Entity join = joinMine.byPersonAndParty(as.getUser(), party);

        joinEditor.setStatus(join, Config.JOIN_STATUS_WITHDRAWN);
        return null;
    }
}
