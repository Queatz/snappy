package com.village.things;

import com.queatz.earth.EarthField;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.as.EarthAs;
import com.queatz.snappy.exceptions.NothingLogicResponse;
import com.queatz.snappy.shared.Config;

public class GoalInterface extends CommonThingInterface {

    @Override
    public EarthThing createThing(EarthAs as) {
        as.requireUser();

        String name = extract(as.getParameters().get(EarthField.NAME));

        if (name == null) {
            throw new NothingLogicResponse("goal - name parameter is expected");
        }

        EarthThing goal = as.s(GoalEditor.class).newGoal(name);

        return goal;
    }

    @Override
    public EarthThing editThing(EarthAs as, EarthThing goal) {
        if (Boolean.toString(true).equals(extract(as.getParameters().get(Config.PARAM_JOIN)))) {
            return join(as, goal);
        } else if (Boolean.toString(false).equals(extract(as.getParameters().get(Config.PARAM_JOIN)))) {
            return leave(as, goal);
        }

        throw new NothingLogicResponse("goal - immutable");
    }

    private EarthThing join(EarthAs as, EarthThing goal) {
        as.requireUser();

        return as.s(JoinEditor.class).newJoin(as.getUser(), goal);
    }

    private EarthThing leave(EarthAs as, EarthThing goal) {
        as.requireUser();

        EarthThing join = as.s(JoinMine.class).byPersonAndParty(as.getUser(), goal);

        return as.s(JoinEditor.class).setStatus(join, Config.JOIN_STATUS_WITHDRAWN);
    }
}
