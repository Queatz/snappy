package com.queatz.snappy.team.actions;

import io.realm.DynamicRealmObject;

/**
 * Created by jacob on 10/7/17.
 */

class ModeChangeAction extends AuthenticatedAction {

    private DynamicRealmObject mode;
    private boolean on;

    public ModeChangeAction(DynamicRealmObject mode, boolean on) {
        this.mode = mode;
        this.on = on;
    }

    @Override
    public void whenAuthenticated() {
        if (on) {
            to(new AddModeRelationshipAction(mode));
        } else {
            to(new RemoveModeRelationshipAction(mode));
        }
    }
}
