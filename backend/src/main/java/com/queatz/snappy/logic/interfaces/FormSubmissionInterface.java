package com.queatz.snappy.logic.interfaces;

import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.snappy.logic.EarthThing;
import com.queatz.snappy.logic.editors.FormItemEditor;
import com.queatz.snappy.logic.editors.FormSubmissionEditor;
import com.queatz.snappy.shared.Config;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by jacob on 6/4/17.
 */

public class FormSubmissionInterface extends CommonLinkInterface {
    @Override
    public EarthThing create(EarthAs as, EarthThing source, EarthThing target, String status, String role) {
        EarthStore earthStore = new EarthStore(as);

        String form = extract(as.getParameters().get(Config.PARAM_IN));

        return new FormSubmissionEditor(as).newFormSubmission(earthStore.get(form), as.getUser());
    }

    @Override
    protected EarthThing edit(EarthAs as, EarthThing link, HttpServletRequest request) {
        return null;
    }
}
