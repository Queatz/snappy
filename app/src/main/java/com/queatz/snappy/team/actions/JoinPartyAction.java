package com.queatz.snappy.team.actions;

import android.widget.Toast;

import com.loopj.android.http.RequestParams;
import com.queatz.snappy.R;
import com.queatz.snappy.Util;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.team.Api;
import com.queatz.snappy.team.Thing;
import com.queatz.snappy.team.ThingKinds;

import io.realm.DynamicRealmObject;

/**
 * Created by jacob on 9/16/17.
 */

public class JoinPartyAction extends AuthenticatedAction {

    private DynamicRealmObject party;

    public JoinPartyAction(String partyId) {
        DynamicRealmObject party = getTeam().realm.where("Thing").equalTo("id", partyId).findFirst();

        if(party == null) {
            getTeam().realm.beginTransaction();
            party = getTeam().realm.createObject("Thing");
            party.setString(Thing.KIND, "party");
            party.setString(Thing.ID, partyId);
            getTeam().realm.commitTransaction();
        }

        this.party = party;
    }

    public JoinPartyAction(DynamicRealmObject party) {
        this.party = party;
    }

    @Override
    public void whenAuthenticated() {
        String localId = null;

        DynamicRealmObject o = getTeam().realm.where("Thing")
                .equalTo("target.id", party.getString(Thing.ID))
                .equalTo("source.id", getTeam().auth.getUser())
                .findFirst();

        if(o == null) {
            localId = Util.createLocalId();

            getTeam().realm.beginTransaction();
            o = getTeam().realm.createObject("Thing");
            o.setString(Thing.KIND, ThingKinds.JOIN);
            o.setString(Thing.ID, localId);
            o.setObject(Thing.SOURCE, getTeam().auth.me());
            o.setObject(Thing.TARGET, party);
            o.setString(Thing.STATUS, Config.JOIN_STATUS_REQUESTED);
            getTeam().realm.commitTransaction();
        }

        RequestParams params = new RequestParams();

        if(localId != null)
            params.put(Config.PARAM_LOCAL_ID, localId);

        params.put(Config.PARAM_JOIN, true);

        getTeam().api.post(Config.PATH_EARTH + "/" + party.getString(Thing.ID), params, new Api.Callback() {
            @Override
            public void success(String response) {
                to(new UpdateThings(response));
            }

            @Override
            public void fail(String response) {
                // Reverse local modifications after retrying
                Toast.makeText(getTeam().context, R.string.join_failed, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
