package com.queatz.snappy.team.actions;

import android.widget.Toast;

import com.loopj.android.http.RequestParams;
import com.queatz.snappy.Util;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.team.Api;
import com.queatz.snappy.team.Thing;

import io.realm.DynamicRealmObject;

/**
 * Created by jacob on 9/16/17.
 */

public class BackThingAction extends ActivityAction {

    private DynamicRealmObject thing;

    public BackThingAction(DynamicRealmObject thing) {
        this.thing = thing;
    }

    @Override
    protected void execute() {
        final String localId = Util.createLocalId();

        getTeam().realm.beginTransaction();
        DynamicRealmObject o = getTeam().realm.createObject("Thing");
        o.setString(Thing.KIND, "follower");
        o.setString(Thing.ID, localId);
        o.setObject(Thing.SOURCE, getTeam().auth.me());
        o.setObject(Thing.TARGET, thing);
        getTeam().realm.commitTransaction();

        RequestParams params = new RequestParams();
        params.put(Config.PARAM_LOCAL_ID, localId);
        params.put(Config.PARAM_FOLLOW, true);

        getTeam().api.post(Config.PATH_EARTH + "/" + thing.getString(Thing.ID), params, new Api.Callback() {
            @Override
            public void success(String response) {
                to(new UpdateThings(response));
            }

            @Override
            public void fail(String response) {
                // Reverse local modifications after retrying
                Toast.makeText(getTeam().context, "Back failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
