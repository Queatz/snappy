package com.queatz.snappy.logic.interfaces;

import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthThing;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by jacob on 6/4/17.
 */

public class FormSubmissionInterface extends CommonLinkInterface {
    @Override
    public EarthThing create(EarthAs as, EarthThing source, EarthThing target, String status, String role) {
        return null;
    }

    @Override
    protected EarthThing edit(EarthAs as, EarthThing link, HttpServletRequest request) {
        return null;
    }
}
