package com.queatz.snappy.logic.interfaces;

import com.google.gson.JsonObject;
import com.queatz.earth.EarthQueries;
import com.queatz.earth.EarthThing;
import com.queatz.earth.FrozenQuery;
import com.queatz.snappy.as.EarthAs;
import com.queatz.snappy.exceptions.NothingLogicResponse;
import com.queatz.snappy.router.Interfaceable;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.shared.EarthJson;
import com.queatz.snappy.view.EarthViewer;
import com.village.things.PersonMine;
import com.vlllage.graph.EarthGraph;

/**
 * Created by jacob on 5/14/16.
 */
public class ByNameInterface implements Interfaceable {

    @Override
    public String get(EarthAs as) {
        switch (as.getRoute().size()) {
            case 2:
                return getPersonByName(as, as.getRoute().get(1));
        }

        throw new NothingLogicResponse("by-name - bad path");
    }

    @Override
    public String post(EarthAs as) {
        return null;
    }

    private String getPersonByName(EarthAs as, String personName) {
        String select = as.getParameters().containsKey(Config.PARAM_SELECT) ? as.getParameters().get(Config.PARAM_SELECT)[0] : null;
        FrozenQuery query = as.s(EarthQueries.class).byGoogleUrl(personName);

        JsonObject json = as.s(EarthGraph.class)
                .queryOne(query.getEarthQuery(), select, query.getVars());

        if (json == null) {
            throw new NothingLogicResponse("by name - nobody");
        }

        return as.s(EarthJson.class).toJson(json);
    }
}
