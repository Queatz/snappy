package com.village.things;

import com.queatz.snappy.as.EarthAs;
import com.queatz.earth.EarthField;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.exceptions.NothingLogicResponse;

/**
 * Created by jacob on 5/23/16.
 */
public class ProjectInterface extends CommonThingInterface {

    @Override
    public EarthThing createThing(EarthAs as) {
        as.requireUser();

        String[] name = as.getParameters().get(EarthField.NAME);

        if (name == null || name.length != 1) {
            throw new NothingLogicResponse("resource - name parameter is expected");
        }

        return new ProjectEditor(as).newProject(name[0], as.getUser());
    }

    @Override
    public EarthThing editThing(EarthAs as, EarthThing resource) {
        as.requireUser();

        String[] name = as.getParameters().get(EarthField.NAME);
        String[] about = as.getParameters().get(EarthField.ABOUT);

        return new ProjectEditor(as).edit(resource, extract(name), extract(about));
    }
}
