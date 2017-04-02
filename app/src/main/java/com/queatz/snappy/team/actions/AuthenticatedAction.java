package com.queatz.snappy.team.actions;


import com.queatz.branch.Branch;
import com.queatz.snappy.team.contexts.TeamContext;

import io.realm.DynamicRealmObject;

/**
 * Created by jacob on 4/1/17.
 */

public abstract class AuthenticatedAction<T extends TeamContext> extends Branch<T> {
    private boolean valid() {
        return me().getTeam().auth.isAuthenticated();
    }

    private void fail() {
        me().getTeam().action.showLoginDialog();
    }

    public abstract void ifAuthenticated();

    public DynamicRealmObject getUser() {
        return me().getTeam().auth.me();
    }

    @Override
    public final void execute() {
        if (valid()) {
            ifAuthenticated();
        } else {
            fail();
        }
    }
}
