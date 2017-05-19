package com.queatz.snappy.adapter;

import android.widget.Toast;

import com.loopj.android.http.RequestParams;
import com.queatz.snappy.Util;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.team.Api;
import com.queatz.snappy.team.Thing;
import com.queatz.snappy.team.actions.AuthenticatedAction;

import java.util.Date;

import io.realm.DynamicRealmObject;

/**
 * Created by jacob on 5/18/17.
 */

public class PostCommentOnAction extends AuthenticatedAction {

    private final DynamicRealmObject update;
    private final String message;

    public PostCommentOnAction(DynamicRealmObject update, String message) {
        this.update = update;
        this.message = message;
    }

    @Override
    public void whenAuthenticated() {
        if (update == null || message == null || message.trim().length() < 1) {
            return;
        }

        final String localId = Util.createLocalId();

        getTeam().realm.beginTransaction();
        DynamicRealmObject o = getTeam().realm.createObject("Thing");
        o.setString(Thing.KIND, "update");
        o.setString(Thing.ID, localId);
        o.setObject(Thing.SOURCE, getTeam().auth.me());
        o.setObject(Thing.TARGET, update);
        o.setString(Thing.ABOUT, message);
        o.setString(Thing.ACTION, Config.UPDATE_ACTION_UPTO);
        o.setDate(Thing.DATE, new Date());
        update.getList(Thing.MEMBERS).add(Util.createMember(o, update));
        getTeam().realm.commitTransaction();

        RequestParams params = new RequestParams();
        params.put(Config.PARAM_THING, update.getString(Thing.ID));
        params.put(Config.PARAM_MESSAGE, message);
        params.put(Config.PARAM_LOCAL_ID, localId);

        // The server expects this for updates
        params.setForceMultipartEntityContentType(true);

        getTeam().api.post(Config.PATH_EARTH + "?kind=update", params, new Api.Callback() {
            @Override
            public void success(String response) {
                getTeam().things.put(response);
            }

            @Override
            public void fail(String response) {
                Toast.makeText(getTeam().context, "Couldn't post comment", Toast.LENGTH_SHORT).show();
            }
        });

    }
}
