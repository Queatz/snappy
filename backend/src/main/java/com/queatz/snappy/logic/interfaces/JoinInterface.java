package com.queatz.snappy.logic.interfaces;

import com.google.cloud.datastore.Entity;
import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.snappy.logic.EarthViewer;
import com.queatz.snappy.logic.concepts.Interfaceable;
import com.queatz.snappy.logic.editors.JoinEditor;
import com.queatz.snappy.logic.exceptions.NothingLogicResponse;
import com.queatz.snappy.logic.views.SuccessView;
import com.queatz.snappy.shared.Config;

/**
 * Created by jacob on 5/14/16.
 */
public class JoinInterface implements Interfaceable {

    @Override
    public String post(EarthAs as) {
        switch (as.getRoute().size()) {
            case 1:
                Entity join = new EarthStore(as).get(as.getRoute().get(0));

                if (join == null) {
                    throw new NothingLogicResponse("join - no");
                }

                // XXX authorize

                if (Boolean.valueOf(as.getRequest().getParameter(Config.PARAM_HIDE))) {
                    new JoinEditor(as).hide(join);
                } else if (Boolean.valueOf(as.getRequest().getParameter(Config.PARAM_ACCEPT))) {
                    new JoinEditor(as).accept(join);
                } else {
                    throw new NothingLogicResponse("join - bad path");
                }

                return new SuccessView(true).toJson();
            default:
                throw new NothingLogicResponse("join - bad path");
        }
    }

    @Override
    public String get(EarthAs as) {
        switch (as.getRoute().size()) {
            case 1:
                Entity join = new EarthStore(as).get(as.getRoute().get(0));

                if (join == null) {
                    throw new NothingLogicResponse("join - no");
                }

                return new EarthViewer(as).getViewForEntityOrThrow(join).toJson();
            default:
                throw new NothingLogicResponse("join - bad path");
        }
    }
}
