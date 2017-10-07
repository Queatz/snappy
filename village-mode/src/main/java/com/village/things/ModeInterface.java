package com.village.things;

import com.queatz.earth.EarthField;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.as.EarthAs;
import com.queatz.snappy.exceptions.NothingLogicResponse;

/**
 * Created by jacob on 10/7/17.
 */

public class ModeInterface extends CommonThingInterface {

    @Override
    public EarthThing createThing(EarthAs as) {
        as.requireUser();

        String name = extract(as.getParameters().get(EarthField.NAME));
        String about = extract(as.getParameters().get(EarthField.ABOUT));

        if (name == null) {
            throw new NothingLogicResponse("mode - name parameter is expected");
        }

        if (about == null) {
            throw new NothingLogicResponse("mode - about parameter is expected");
        }

        EarthThing resource = as.s(ModeEditor.class).newMode(name, about, as.getUser());
        resource = postPhoto(resource, as);

        return resource;
    }

    @Override
    public EarthThing editThing(EarthAs as, EarthThing resource) {
        throw new NothingLogicResponse("mode - no");
    }
}
