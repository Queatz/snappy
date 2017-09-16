package com.queatz.snappy.team.actions;

import android.widget.Toast;

import com.loopj.android.http.RequestParams;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.team.Api;
import com.queatz.snappy.team.Thing;

import io.realm.DynamicRealmObject;

/**
 * Created by jacob on 9/16/17.
 */

public class MarkPartyFullAction extends AuthenticatedAction {

    private DynamicRealmObject party;

    public MarkPartyFullAction(DynamicRealmObject party) {
        this.party = party;
    }

    @Override
    public void whenAuthenticated() {
        getTeam().realm.beginTransaction();
        party.setBoolean(Thing.FULL, true);
        getTeam().realm.commitTransaction();

        RequestParams params = new RequestParams();
        params.put(Config.PARAM_FULL, true);

        getTeam().api.post(Config.PATH_EARTH + "/" + party.getString(Thing.ID), params, new Api.Callback() {
            @Override
            public void success(String response) {
            }

            @Override
            public void fail(String response) {
                // Reverse local modifications after retrying
                Toast.makeText(getTeam().context, "Mark full failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
