package com.queatz.snappy.team.actions;

import android.widget.Toast;

import com.loopj.android.http.RequestParams;
import com.queatz.snappy.R;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.team.Api;
import com.queatz.snappy.team.Thing;

import io.realm.DynamicRealmObject;
import io.realm.RealmResults;

/**
 * Created by jacob on 9/16/17.
 */

public class CancelJoinAction extends AuthenticatedAction {

    private DynamicRealmObject party;

    public CancelJoinAction(DynamicRealmObject party) {
        this.party = party;
    }

    @Override
    public void whenAuthenticated() {
        RealmResults<DynamicRealmObject> joins = getTeam().realm.where("Thing")
                .equalTo("source.id", getTeam().auth.getUser())
                .equalTo("target.id", party.getString(Thing.ID))
                .findAll();

        getTeam().realm.beginTransaction();

        for(int i = 0; i < joins.size(); i++) {
            DynamicRealmObject join = joins.get(i);
            join.setString(Thing.STATUS, Config.JOIN_STATUS_WITHDRAWN);
        }

        getTeam().realm.commitTransaction();

        RequestParams params = new RequestParams();
        params.put(Config.PARAM_CANCEL_JOIN, true);

        getTeam().api.post(Config.PATH_EARTH + "/" + party.getString(Thing.ID), params, new Api.Callback() {
            @Override
            public void success(String response) {
            }

            @Override
            public void fail(String response) {
                // Reverse local modifications after retrying
                Toast.makeText(getTeam().context, R.string.offer_cancel_failed, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
