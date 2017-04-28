package com.queatz.snappy.logic.interfaces;

import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.snappy.logic.EarthThing;
import com.queatz.snappy.logic.EarthViewer;
import com.queatz.snappy.logic.concepts.Interfaceable;
import com.queatz.snappy.logic.editors.MemberEditor;
import com.queatz.snappy.logic.exceptions.NothingLogicResponse;
import com.queatz.snappy.logic.views.SuccessView;
import com.queatz.snappy.shared.Config;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by jacob on 4/9/17.
 *
 * Common Link Interface
 *
 * Supports creating, editing, and deleting.
 *
 * Create = POST /api?kind=<link>&source=<thing>&target=<thing>[&role=<string>]
 * Edit = POST /12345[?status=in]
 * Delete = POST /12345/delete
 */

public abstract class CommonLinkInterface implements Interfaceable {

    public abstract EarthThing create(EarthAs as, EarthThing source, EarthThing target, String status, String role);
    protected abstract EarthThing edit(EarthAs as, EarthThing link, HttpServletRequest request);

    @Override
    public String get(EarthAs as) {
        switch (as.getRoute().size()) {
            case 0:
                throw new NothingLogicResponse("link - empty route");
            case 1:
                EarthThing thing = new EarthStore(as).get(as.getRoute().get(0));

                return new EarthViewer(as).getViewForEntityOrThrow(thing).toJson();
            default:
                throw new NothingLogicResponse("link - bad path");
        }
    }

    @Override
    public String post(EarthAs as) {
        as.requireUser();

        EarthStore earthStore = new EarthStore(as);

        switch (as.getRoute().size()) {
            case 0: {
                EarthThing source = earthStore.get(as.getParameters().get(Config.PARAM_SOURCE)[0]);
                EarthThing target = earthStore.get(as.getParameters().get(Config.PARAM_TARGET)[0]);
                String role = extract(as.getParameters().get(Config.PARAM_ROLE));

                EarthThing link = create(as, source, target, Config.MEMBER_STATUS_ACTIVE, role);
                new MemberEditor(as).create(link, target, Config.MEMBER_STATUS_ACTIVE);

                return new EarthViewer(as).getViewForEntityOrThrow(link).toJson();
            }
            case 1: {
                EarthThing link = earthStore.get(as.getRoute().get(0));

                link = edit(as, link, as.getRequest());

                return new EarthViewer(as).getViewForEntityOrThrow(link).toJson();
            }

            case 2: {
                if (Config.PATH_DELETE.equals(as.getRoute().get(1))) {
                    EarthThing thing = earthStore.get(as.getRoute().get(0));
                    earthStore.conclude(thing);
                    return new SuccessView(true).toJson();
                }

                throw new NothingLogicResponse("link - bad path");
            }
        }

        return null;
    }

    protected String extract(String[] param) {
        return param == null || param.length != 1 ? null : param[0];
    }
}
