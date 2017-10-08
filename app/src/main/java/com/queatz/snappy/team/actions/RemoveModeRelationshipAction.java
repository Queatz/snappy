package com.queatz.snappy.team.actions;

import android.widget.Toast;

import com.queatz.snappy.Util;
import com.queatz.snappy.adapter.DeleteThingAction;

import io.realm.DynamicRealmObject;

/**
 * Created by jacob on 10/7/17.
 */

class RemoveModeRelationshipAction extends AuthenticatedAction {

    private final DynamicRealmObject mode;

    public RemoveModeRelationshipAction(DynamicRealmObject mode) {
        this.mode = mode;
    }

    @Override
    public void whenAuthenticated() {
        DynamicRealmObject member = Util.getModeMember(mode, getUser());

        if (member == null) {
            Toast.makeText(getTeam().context, "Turn off mode failed", Toast.LENGTH_SHORT).show();
            return;
        }

        to(new DeleteThingAction(member));
    }
}
