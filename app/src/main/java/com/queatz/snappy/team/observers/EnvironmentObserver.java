package com.queatz.snappy.team.observers;

import com.queatz.snappy.team.Environment;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by jacob on 4/2/17.
 */

public class EnvironmentObserver {

    private final Environment environment;

    public EnvironmentObserver(Environment environment) {
        this.environment = environment;
    }

    private final Map<Class<? extends CurrentEnvironment>, Set<CurrentEnvironment>> changes = new HashMap<>();

    @SuppressWarnings("unchecked")
    public <T extends CurrentEnvironment> void when(T change) {
        on(change);

        if (environment.interpret((Class<? extends CurrentEnvironment>) change.getClass().getInterfaces()[0])) {
            change.then();
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends CurrentEnvironment> void on(T change) {
        Class<? extends CurrentEnvironment> type = (Class<? extends CurrentEnvironment>) change.getClass().getInterfaces()[0];

        if (!changes.containsKey(type)) {
            changes.put(type, new HashSet<CurrentEnvironment>());
        }

        changes.get(type).add(change);
    }

    public <T extends CurrentEnvironment> void off(T change) {
        changes.get(change.getClass().getInterfaces()[0]).remove(change);
    }

    public void handle(Class<? extends CurrentEnvironment> change) {
        if (!changes.containsKey(change)) {
            return;
        }

        if (!environment.interpret(change)) {
            return;
        }

        for (CurrentEnvironment environment : changes.get(change)) {
            environment.then();
        }
    }
}
