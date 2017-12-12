package com.village.things;

import com.queatz.earth.EarthField;
import com.queatz.earth.EarthKind;
import com.queatz.earth.EarthStore;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.as.EarthAs;
import com.queatz.snappy.as.EarthControl;

import org.jetbrains.annotations.NotNull;

/**
 * Created by jacob on 12/12/17.
 */

public class GoalEditor extends EarthControl {
    public GoalEditor(@NotNull EarthAs as) {
        super(as);
    }

    public EarthThing newGoal(@NotNull String name) {
        EarthStore earthStore = use(EarthStore.class);

        EarthThing goal = earthStore.save(earthStore.edit(earthStore.create(EarthKind.GOAL_KIND))
                .set(EarthField.NAME, name));

        

        return goal;
    }
}
