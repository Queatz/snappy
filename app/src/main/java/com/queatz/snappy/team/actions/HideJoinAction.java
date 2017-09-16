package com.queatz.snappy.team.actions;

import android.widget.Toast;

import com.loopj.android.http.RequestParams;
import com.queatz.snappy.R;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.team.Api;
import com.queatz.snappy.team.Thing;
import com.queatz.snappy.util.ResponseUtil;

import io.realm.DynamicRealmObject;

/**
 * Created by jacob on 9/16/17.
 */

public class HideJoinAction extends AuthenticatedAction {

    private DynamicRealmObject join;

    public HideJoinAction(DynamicRealmObject join) {
        this.join = join;
    }

    @Override
    public void whenAuthenticated() {
        getTeam().realm.beginTransaction();
        join.setString(Thing.STATUS, Config.JOIN_STATUS_OUT);
        getTeam().realm.commitTransaction();

        RequestParams params = new RequestParams();
        params.put(Config.PARAM_HIDE, true);

        getTeam().api.post(Config.PATH_EARTH + "/" + join.getString(Thing.ID), params, new Api.Callback() {
            @Override
            public void success(String response) {
                if (!ResponseUtil.isSuccess(response)) {
                    Toast.makeText(getTeam().context, R.string.hide_join_failed, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void fail(String response) {
                // Reverse local modifications after retrying
                Toast.makeText(getTeam().context, R.string.hide_join_failed, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
