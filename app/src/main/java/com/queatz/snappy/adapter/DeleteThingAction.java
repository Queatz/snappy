package com.queatz.snappy.adapter;

import com.queatz.snappy.shared.Config;
import com.queatz.snappy.team.Thing;
import com.queatz.snappy.team.actions.AuthenticatedAction;

import io.realm.DynamicRealmObject;

/**
 * Created by jacob on 5/18/17.
 */

public class DeleteThingAction extends AuthenticatedAction {
    private final DynamicRealmObject thing;

    public DeleteThingAction(DynamicRealmObject thing) {
        this.thing = thing;
    }

    @Override
    public void whenAuthenticated() {
        // TODO keep until delete is in place
        try {
            getTeam().api.post(Config.PATH_EARTH + "/" + thing.getString(Thing.ID) + "/" + Config.PATH_DELETE);

            getTeam().realm.beginTransaction();
            thing.deleteFromRealm();
            getTeam().realm.commitTransaction();
        }
        catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }
}
