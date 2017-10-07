package com.village.things;

import com.queatz.earth.EarthField;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.as.EarthAs;
import com.queatz.snappy.shared.Config;

/**
 * Created by jacob on 6/4/17.
 */

public class FormInterface extends CommonThingInterface {
    @Override
    public EarthThing createThing(EarthAs as) {
        String name = extract(as.getParameters().get(Config.PARAM_NAME));

        return as.s(FormEditor.class).newForm(as.getUser(), name);
    }

    @Override
    public EarthThing editThing(EarthAs as, EarthThing thing) {
        as.requireUser();

        String name = extract(as.getParameters().get(EarthField.NAME));
        String about = extract(as.getParameters().get(EarthField.ABOUT));
        String data = extract(as.getParameters().get(EarthField.DATA));

        return as.s(FormEditor.class).edit(thing, name, about, data);
    }
}
