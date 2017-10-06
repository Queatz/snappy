package com.queatz.snappy.logic.interfaces;

import com.queatz.snappy.as.EarthAs;
import com.queatz.earth.EarthField;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.logic.editors.ResourceEditor;
import com.queatz.snappy.exceptions.NothingLogicResponse;
import com.village.things.CommonThingInterface;

/**
 * Created by jacob on 5/22/16.
 */
public class ResourceInterface extends CommonThingInterface {

    @Override
    public EarthThing createThing(EarthAs as) {
        as.requireUser();

        String[] name = as.getParameters().get(EarthField.NAME);

        if (name == null || name.length != 1) {
            throw new NothingLogicResponse("resource - name parameter is expected");
        }

        EarthThing resource = new ResourceEditor(as).newResource(name[0], as.getUser());
        resource = postPhoto(resource, as);

        return resource;
    }

    @Override
    public EarthThing editThing(EarthAs as, EarthThing resource) {
        as.requireUser();

        String[] name = as.getParameters().get(EarthField.NAME);
        String[] about = as.getParameters().get(EarthField.ABOUT);

        return new ResourceEditor(as).edit(resource, extract(name), extract(about));
    }
}
