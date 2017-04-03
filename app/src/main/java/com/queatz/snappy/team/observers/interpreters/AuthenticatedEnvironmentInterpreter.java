package com.queatz.snappy.team.observers.interpreters;

import com.queatz.snappy.team.Team;

/**
 * Created by jacob on 4/2/17.
 */

public class AuthenticatedEnvironmentInterpreter implements EnvironmentInterpreter {
    @Override
    public boolean interpret(Team team) {
        return team.auth.isAuthenticated();
    }
}
