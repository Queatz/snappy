package com.queatz.snappy.logic.interfaces;

import com.google.cloud.datastore.Entity;
import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthSingleton;
import com.queatz.snappy.logic.editors.ProjectEditor;
import com.queatz.snappy.logic.exceptions.NothingLogicResponse;

/**
 * Created by jacob on 5/23/16.
 */
public class ProjectInterface extends CommonThingInterface {

    private final ProjectEditor projectEditor = EarthSingleton.of(ProjectEditor.class);

    @Override
    public Entity createThing(EarthAs as) {
        String[] name = as.getParameters().get(EarthField.NAME);

        if (name == null || name.length != 1) {
            throw new NothingLogicResponse("resource - name parameter is expected");
        }

        return projectEditor.newProject(name[0], as.getUser());
    }

    @Override
    public Entity editThing(EarthAs as, Entity resource) {
        String[] name = as.getParameters().get(EarthField.NAME);
        String[] about = as.getParameters().get(EarthField.ABOUT);

        return projectEditor.edit(resource, extract(name), extract(about));
    }

    private String extract(String[] param) {
        return param == null || param.length != 1 ? null : param[0];
    }
}
