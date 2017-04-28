package com.queatz.snappy.logic.interfaces;

import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthThing;
import com.queatz.snappy.logic.editors.MemberEditor;
import com.queatz.snappy.logic.exceptions.NothingLogicResponse;
import com.queatz.snappy.shared.Config;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by jacob on 4/9/17.
 */

public class MemberInterface extends CommonLinkInterface {
    @Override
    public EarthThing create(EarthAs as, EarthThing source, EarthThing target, String status, String role) {
        return new MemberEditor(as).create(source, target, status, role);
    }

    @Override
    protected EarthThing edit(EarthAs as, EarthThing link, HttpServletRequest request) {
        as.requireUser();

        switch (as.getRoute().size()) {
            case 1:
                String role = extract(as.getParameters().get(Config.PARAM_ROLE));
                return new MemberEditor(as).editRole(link, role);
        }

        throw new NothingLogicResponse("member - bad path");
    }
}
