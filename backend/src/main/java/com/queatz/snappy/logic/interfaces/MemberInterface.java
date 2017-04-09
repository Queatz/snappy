package com.queatz.snappy.logic.interfaces;

import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthThing;
import com.queatz.snappy.logic.editors.MemberEditor;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by jacob on 4/9/17.
 */

public class MemberInterface extends CommonLinkInterface {
    @Override
    public EarthThing create(EarthAs as, EarthThing source, EarthThing target, String role) {
        return new MemberEditor(as).create(source, target, role);
    }

    @Override
    protected EarthThing edit(EarthAs as, EarthThing link, HttpServletRequest request) {
        return link; // no-op
    }
}
