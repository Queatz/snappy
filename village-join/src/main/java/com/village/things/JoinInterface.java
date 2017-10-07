package com.village.things;

import com.queatz.snappy.as.EarthAs;
import com.queatz.earth.EarthField;
import com.queatz.earth.EarthStore;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.events.EarthUpdate;
import com.queatz.snappy.view.EarthViewer;
import com.queatz.snappy.router.Interfaceable;
import com.queatz.snappy.exceptions.NothingLogicResponse;
import com.queatz.snappy.view.SuccessView;
import com.queatz.snappy.shared.Config;

/**
 * Created by jacob on 5/14/16.
 */
public class JoinInterface implements Interfaceable {

    @Override
    public String post(EarthAs as) {
        switch (as.getRoute().size()) {
            case 1:
                EarthThing join = new EarthStore(as).get(as.getRoute().get(0));

                if (join == null) {
                    throw new NothingLogicResponse("join - no");
                }

                // XXX authorize

                if (Boolean.valueOf(as.getRequest().getParameter(Config.PARAM_HIDE))) {
                    new JoinEditor(as).hide(join);
                } else if (Boolean.valueOf(as.getRequest().getParameter(Config.PARAM_ACCEPT))) {
                    new JoinEditor(as).accept(join);

                    new EarthUpdate(as).send(new JoinAcceptedEvent(join))
                            .to(join.getKey(EarthField.SOURCE));
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
                EarthThing join = new EarthStore(as).get(as.getRoute().get(0));

                if (join == null) {
                    throw new NothingLogicResponse("join - no");
                }

                return new EarthViewer(as).getViewForEntityOrThrow(join).toJson();
            default:
                throw new NothingLogicResponse("join - bad path");
        }
    }
}
