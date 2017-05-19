package com.queatz.snappy.team.actions;


import com.queatz.branch.Branch;
import com.queatz.snappy.team.contexts.ActivityContext;

import io.realm.DynamicRealmObject;

/**
 * Created by jacob on 4/1/17.
 */

public abstract class AuthenticatedAction extends ActivityAction {
    private boolean valid() {
        return me().getTeam().auth.isAuthenticated();
    }

    private void fail() {
        to(new SigninAction());
    }

    public abstract void whenAuthenticated();

    public void otherwise() {

    }

    public DynamicRealmObject getUser() {
        return me().getTeam().auth.me();
    }

    @Override
    public final void execute() {
        if (valid()) {
            whenAuthenticated();
        } else {
            fail();
            otherwise();
        }
    }
}
