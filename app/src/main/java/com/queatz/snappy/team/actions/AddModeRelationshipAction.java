package com.queatz.snappy.team.actions;

import android.widget.Toast;

import com.loopj.android.http.RequestParams;
import com.queatz.snappy.R;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.team.Api;
import com.queatz.snappy.team.Thing;
import com.queatz.snappy.team.ThingKinds;

import io.realm.DynamicRealmObject;

/**
 * Created by jacob on 10/7/17.
 */

class AddModeRelationshipAction extends AuthenticatedAction {

    private final DynamicRealmObject mode;

    public AddModeRelationshipAction(DynamicRealmObject mode) {
        this.mode = mode;
    }

    @Override
    public void whenAuthenticated() {
        RequestParams params = new RequestParams();
        params.put(Config.PARAM_KIND, ThingKinds.MEMBER);
//        params.put(Config.PARAM_LOCAL_ID, mode.getString(Thing.ID));
        params.put(Config.PARAM_SOURCE, mode.getString(Thing.ID));
        params.put(Config.PARAM_TARGET, getUser().getString(Thing.ID));

        me().getTeam().api.post(Config.PATH_EARTH, params, new Api.Callback() {
            @Override
            public void success(String response) {
                to(new UpdateThings(response));
            }

            @Override
            public void fail(String response) {
                Toast.makeText(me().getTeam().context, R.string.couldnt_add_mode, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
