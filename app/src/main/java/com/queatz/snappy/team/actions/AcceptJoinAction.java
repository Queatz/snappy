package com.queatz.snappy.team.actions;

import android.widget.Toast;

import com.queatz.snappy.shared.Config;
import com.queatz.snappy.team.Api;
import com.queatz.snappy.team.Thing;
import com.queatz.snappy.team.ThingKinds;

import io.realm.DynamicRealmObject;

/**
 * Created by jacob on 9/16/17.
 */

public class AcceptJoinAction extends AuthenticatedAction {

    private DynamicRealmObject join;

    public AcceptJoinAction(String joinId) {
        DynamicRealmObject join = getTeam().realm.where("Thing").equalTo(Thing.ID, joinId).findFirst();

        if(join == null) {
            getTeam().realm.beginTransaction();
            join = getTeam().realm.createObject("Thing");
            join.setString(Thing.KIND, ThingKinds.JOIN);
            join.setString(Thing.ID, joinId);
            join.setString(Thing.STATUS, Config.JOIN_STATUS_REQUESTED);
            getTeam().realm.commitTransaction();
        }

        this.join = join;
    }

    public AcceptJoinAction(DynamicRealmObject join) {
        this.join = join;
    }

    @Override
    public void whenAuthenticated() {
        getTeam().realm.beginTransaction();
        join.setString(Thing.STATUS, Config.JOIN_STATUS_IN);
        getTeam().realm.commitTransaction();

        getTeam().earth.acceptJoin(join, new Api.Callback() {
            @Override
            public void success(String response) {
                getTeam().push.clear("join/" + join.getString(Thing.ID) + "/request");
            }

            @Override
            public void fail(String response) {
                // Reverse local modifications after retrying
                Toast.makeText(getTeam().context, "Accept failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
