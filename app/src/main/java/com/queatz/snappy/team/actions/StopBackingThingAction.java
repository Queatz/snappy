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

public class StopBackingThingAction extends ActivityAction {

    private DynamicRealmObject thing;

    public StopBackingThingAction(DynamicRealmObject thing) {
        this.thing = thing;
    }

    @Override
    protected void execute() {
        DynamicRealmObject follow = getTeam().realm.where("Thing")
                .equalTo(Thing.KIND, "follower")
                .equalTo("source.id", getTeam().auth.getUser())
                .equalTo("target.id", thing.getString(Thing.ID))
                .findFirst();

        if(follow != null) {
            getTeam().realm.beginTransaction();
            follow.deleteFromRealm();
            getTeam().realm.commitTransaction();
        }

        RequestParams params = new RequestParams();
        params.put(Config.PARAM_FOLLOW, false);

        getTeam().api.post(Config.PATH_EARTH + "/" + thing.getString(Thing.ID), params, new Api.Callback() {
            @Override
            public void success(String response) {
            }

            @Override
            public void fail(String response) {
                // Tollback local deletion
                Toast.makeText(getTeam().context, "Stop following failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
