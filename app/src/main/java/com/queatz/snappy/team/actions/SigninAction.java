package com.queatz.snappy.team.actions;

import com.queatz.snappy.team.Auth;

/**
 * Created by jacob on 4/3/17.
 */

public class SigninAction extends ActivityAction {
    @Override
    protected void execute() {
        Auth auth = getTeam().auth;
        auth.setActivity(me().getActivity());
        auth.signin();
    }
}
