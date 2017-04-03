package com.queatz.snappy.team;

import com.queatz.snappy.team.observers.AnonymousEnvironment;
import com.queatz.snappy.team.observers.AuthenticatedEnvironment;
import com.queatz.snappy.team.observers.CurrentEnvironment;
import com.queatz.snappy.team.observers.EnvironmentObserver;
import com.queatz.snappy.team.observers.interpreters.AnonymousEnvironmentInterpreter;
import com.queatz.snappy.team.observers.interpreters.AuthenticatedEnvironmentInterpreter;
import com.queatz.snappy.team.observers.interpreters.EnvironmentInterpreter;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jacob on 4/2/17.
 */

public class Environment {

    private final Team team;
    private final Map<Object, EnvironmentObserver> observers = new HashMap<>();
    private final Map<Class<? extends CurrentEnvironment>, EnvironmentInterpreter> interpreters = new HashMap<>();

    public Environment(Team team) {
        this.team = team;
        initInterpreters();
    }

    public EnvironmentObserver observe(Object environmentContext) {
        EnvironmentObserver environmentObserver = new EnvironmentObserver(this);
        observers.put(environmentContext, environmentObserver);
        return environmentObserver;
    }

    public void forget(Object environmentContext) {
        observers.remove(environmentContext);
    }

    public void handle(Class<? extends CurrentEnvironment> change) {
        synchronized (observers) {
            for (EnvironmentObserver observer : observers.values()) {
                observer.handle(change);
            }
        }
    }

    public boolean interpret(Class<? extends CurrentEnvironment> change) {
        if (!interpreters.containsKey(change)) {
            return false;
        }

        return interpreters.get(change).interpret(team);
    }

    private void initInterpreters() {
        interpreters.put(AnonymousEnvironment.class     , new AnonymousEnvironmentInterpreter());
        interpreters.put(AuthenticatedEnvironment.class , new AuthenticatedEnvironmentInterpreter());
    }

    public boolean is(Class<? extends CurrentEnvironment> environment) {
        return interpret(environment);
    }
}
