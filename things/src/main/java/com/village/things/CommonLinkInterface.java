package com.village.things;

import com.queatz.earth.EarthStore;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.as.EarthAs;
import com.queatz.snappy.exceptions.NothingLogicResponse;
import com.queatz.snappy.plugins.MemberEditorPlugin;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.view.EarthViewer;
import com.queatz.snappy.view.SuccessView;

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

public abstract class CommonLinkInterface extends ExistenceInterface {

    public abstract EarthThing create(EarthAs as, EarthThing source, EarthThing target, String status, String role);
    protected abstract EarthThing edit(EarthAs as, EarthThing link, HttpServletRequest request);

    @Override
    public String get(EarthAs as) {
        switch (as.getRoute().size()) {
            case 0:
                throw new NothingLogicResponse("link - empty route");
            case 1:
                EarthThing thing = as.s(EarthStore.class).get(as.getRoute().get(0));

                return returnIfGraph(as, thing);
            default:
                throw new NothingLogicResponse("link - bad path");
        }
    }

    @Override
    public String post(EarthAs as) {
        as.requireUser();

        EarthStore earthStore = as.s(EarthStore.class);

        switch (as.getRoute().size()) {
            case 0: {
                EarthThing source = earthStore.get(extract(as.getParameters().get(Config.PARAM_SOURCE)));
                EarthThing target = earthStore.get(extract(as.getParameters().get(Config.PARAM_TARGET)));

                if (source == null || target == null) {
                    return new SuccessView(false).toJson();
                }

                String role = extract(as.getParameters().get(Config.PARAM_ROLE));

                EarthThing link = create(as, source, target, Config.MEMBER_STATUS_ACTIVE, role);
                as.s(MemberEditorPlugin.class).create(link, target, Config.MEMBER_STATUS_ACTIVE);

                setVisibility(as, link);

                return returnIfGraph(as, link);
            }
            case 1: {
                EarthThing link = earthStore.get(as.getRoute().get(0));

                link = edit(as, link, as.getRequest());

                setVisibility(as, link);

                return returnIfGraph(as, link);
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
