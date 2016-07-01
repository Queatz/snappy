package com.queatz.snappy.logic.interfaces;

import com.google.cloud.datastore.Entity;
import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthSingleton;
import com.queatz.snappy.logic.editors.ResourceEditor;
import com.queatz.snappy.logic.exceptions.NothingLogicResponse;

/**
 * Created by jacob on 5/22/16.
 */
public class ResourceInterface extends CommonThingInterface {

    private final ResourceEditor resourceEditor = EarthSingleton.of(ResourceEditor.class);

    @Override
    public Entity createThing(EarthAs as) {
        String[] name = as.getParameters().get(EarthField.NAME);

        if (name == null || name.length != 1) {
            throw new NothingLogicResponse("resource - name parameter is expected");
        }

        Entity resource = resourceEditor.newResource(name[0], as.getUser());
        resource = postPhoto(resource, as);

        return resource;
    }

    @Override
    public Entity editThing(EarthAs as, Entity resource) {
        String[] name = as.getParameters().get(EarthField.NAME);
        String[] about = as.getParameters().get(EarthField.ABOUT);

        return resourceEditor.edit(resource, extract(name), extract(about));
    }

    private String extract(String[] param) {
        return param == null || param.length != 1 ? null : param[0];
    }
}
