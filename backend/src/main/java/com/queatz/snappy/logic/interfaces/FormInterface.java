package com.queatz.snappy.logic.interfaces;

import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthThing;
import com.queatz.snappy.logic.editors.FormEditor;
import com.queatz.snappy.logic.editors.ProjectEditor;
import com.queatz.snappy.shared.Config;

/**
 * Created by jacob on 6/4/17.
 */

public class FormInterface extends CommonThingInterface {
    @Override
    public EarthThing createThing(EarthAs as) {
        String name = extract(as.getParameters().get(Config.PARAM_NAME));

        return new FormEditor(as).newForm(as.getUser(), name);
    }

    @Override
    public EarthThing editThing(EarthAs as, EarthThing thing) {
        as.requireUser();

        String[] name = as.getParameters().get(EarthField.NAME);
        String[] about = as.getParameters().get(EarthField.ABOUT);

        return new ProjectEditor(as).edit(thing, extract(name), extract(about));
    }
}
