package com.village.things;

import com.queatz.earth.EarthField;
import com.queatz.earth.EarthQueries;
import com.queatz.earth.EarthStore;
import com.queatz.earth.EarthThing;
import com.queatz.earth.FrozenQuery;
import com.queatz.snappy.as.EarthAs;
import com.queatz.snappy.events.EarthUpdate;
import com.queatz.snappy.exceptions.NothingLogicResponse;
import com.queatz.snappy.router.Interfaceable;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.shared.EarthJson;
import com.queatz.snappy.view.SuccessView;
import com.vlllage.graph.EarthGraph;

/**
 * Created by jacob on 5/14/16.
 */
public class JoinInterface implements Interfaceable {

    @Override
    public String post(EarthAs as) {
        switch (as.getRoute().size()) {
            case 1:
                EarthThing join = as.s(EarthStore.class).get(as.getRoute().get(0));

                if (join == null) {
                    throw new NothingLogicResponse("join - no");
                }

                // XXX authorize

                if (Boolean.valueOf(as.getRequest().getParameter(Config.PARAM_HIDE))) {
                    as.s(JoinEditor.class).hide(join);
                } else if (Boolean.valueOf(as.getRequest().getParameter(Config.PARAM_ACCEPT))) {
                    as.s(JoinEditor.class).accept(join);

                    as.s(EarthUpdate.class).send(new JoinAcceptedEvent(join))
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
                String select = as.getParameters().containsKey(Config.PARAM_SELECT) ? as.getParameters().get(Config.PARAM_SELECT)[0] : null;
                FrozenQuery query = as.s(EarthQueries.class).byId(as.getRoute().get(0));

                return as.s(EarthJson.class).toJson(
                        as.s(EarthGraph.class).queryOne(query.getEarthQuery(), select, query.getVars())
                );
            default:
                throw new NothingLogicResponse("join - bad path");
        }
    }
}
