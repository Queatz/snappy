package com.queatz.snappy.team.actions;

import android.widget.Toast;

import com.loopj.android.http.RequestParams;
import com.queatz.snappy.Util;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.team.Api;
import com.queatz.snappy.team.Thing;

import io.realm.DynamicRealmObject;

/**
 * Created by jacob on 4/4/17.
 */

public class LikeUpdateAction extends AuthenticatedAction {

    private DynamicRealmObject update;

    public LikeUpdateAction(DynamicRealmObject update) {
        this.update = update;
    }

    @Override
    public void whenAuthenticated() {
        if (Util.liked(update, getUser())) {
            return;
        }

        String localId = Util.createLocalId();

        me().getTeam().realm.beginTransaction();
        DynamicRealmObject o = me().getTeam().realm.createObject("Thing");
        o.setString(Thing.KIND, "like");
        o.setString(Thing.ID, localId);
        o.setObject(Thing.SOURCE, getUser());
        o.setObject(Thing.TARGET, update);
        me().getTeam().realm.commitTransaction();

        RequestParams params = new RequestParams();
        params.put(Config.PARAM_LOCAL_ID, localId);

        me().getTeam().api.post(Config.PATH_EARTH + "/" + update.getString(Thing.ID) + "/" + Config.PATH_LIKE, params, new Api.Callback() {
            @Override
            public void success(String response) {
                me().getTeam().things.put(response);
            }

            @Override
            public void fail(String response) {
                Toast.makeText(me().getTeam().context, "Couldn't like " + update.getString(Thing.KIND), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
