package com.village.things;

import com.queatz.earth.EarthStore;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.as.EarthAs;
import com.queatz.snappy.shared.Config;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by jacob on 6/4/17.
 */

public class FormItemInterface extends CommonLinkInterface {
    @Override
    public EarthThing create(EarthAs as, EarthThing source, EarthThing target, String status, String role) {
        EarthStore earthStore = as.s(EarthStore.class);

        String form = extract(as.getParameters().get(Config.PARAM_IN));
        String type = extract(as.getParameters().get(Config.PARAM_TYPE));

        return as.s(FormItemEditor.class).newFormItem(earthStore.get(form), type);
    }

    @Override
    protected EarthThing edit(EarthAs as, EarthThing link, HttpServletRequest request) {
        return null;
    }
}
