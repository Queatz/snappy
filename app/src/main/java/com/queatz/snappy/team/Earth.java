package com.queatz.snappy.team;

import com.loopj.android.http.RequestParams;
import com.queatz.snappy.shared.Config;

import io.realm.DynamicRealmObject;

public class Earth {

    private final Team team;

    public Earth(Team team) {
        this.team = team;
    }

    public void acceptJoin(DynamicRealmObject join, Api.Callback callback) {
        RequestParams params = new RequestParams();
        params.put(Config.PARAM_ACCEPT, true);
        team.api.post(Config.PATH_EARTH + "/" + join.getString(Thing.ID), params, callback);
    }
}
