package com.queatz.snappy.logic.interfaces;

import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.logic.editors.FormItemEditor;
import com.queatz.snappy.shared.Config;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by jacob on 6/4/17.
 */

public class FormItemInterface extends CommonLinkInterface {
    @Override
    public EarthThing create(EarthAs as, EarthThing source, EarthThing target, String status, String role) {
        EarthStore earthStore = new EarthStore(as);

        String form = extract(as.getParameters().get(Config.PARAM_IN));
        String type = extract(as.getParameters().get(Config.PARAM_TYPE));

        return new FormItemEditor(as).newFormItem(earthStore.get(form), type);
    }

    @Override
    protected EarthThing edit(EarthAs as, EarthThing link, HttpServletRequest request) {
        return null;
    }
}
