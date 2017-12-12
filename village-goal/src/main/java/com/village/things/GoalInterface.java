package com.village.things;

import com.queatz.earth.EarthField;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.as.EarthAs;
import com.queatz.snappy.exceptions.NothingLogicResponse;

public class GoalInterface extends CommonThingInterface {

    @Override
    public EarthThing createThing(EarthAs as) {
        as.requireUser();

        String name = extract(as.getParameters().get(EarthField.NAME));

        if (name == null) {
            throw new NothingLogicResponse("goal - name parameter is expected");
        }

        return as.s(GoalEditor.class).newGoal(name);
    }

    @Override
    public EarthThing editThing(EarthAs as, EarthThing goal) {
        throw new NothingLogicResponse("goal - immutable");
    }
}
