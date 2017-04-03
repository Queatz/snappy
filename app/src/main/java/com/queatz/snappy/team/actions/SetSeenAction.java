package com.queatz.snappy.team.actions;


import com.loopj.android.http.RequestParams;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.team.Thing;

import io.realm.DynamicRealmObject;
import io.realm.RealmResults;

/**
 * Created by jacob on 4/1/17.
 */

public class SetSeenAction extends AuthenticatedAction {

    private final DynamicRealmObject person;

    public SetSeenAction(DynamicRealmObject person) {
        this.person = person;
    }

    @Override
    public void whenAuthenticated() {
        RealmResults<DynamicRealmObject> recents = me().getTeam().realm.where("Thing")
                .equalTo("source.id", getUser().getString(Thing.ID))
                .equalTo("target.id", person.getString(Thing.ID))
                .findAll();

        boolean changed = false;

        me().getTeam().realm.beginTransaction();

        for(int i = 0; i < recents.size(); i++) {
            DynamicRealmObject recent = recents.get(i);

            if(!recent.getBoolean(Thing.SEEN)) {
                recent.setBoolean(Thing.SEEN, true);
                changed = true;
            }
        }

        if(changed) {
            me().getTeam().realm.commitTransaction();

            RequestParams params = new RequestParams();
            params.put(Config.PARAM_SEEN, true);

            me().getTeam().api.post(Config.PATH_EARTH + "/" + person.getString(Thing.ID), params);
        }
        else {
            me().getTeam().realm.cancelTransaction();
        }
    }
}
