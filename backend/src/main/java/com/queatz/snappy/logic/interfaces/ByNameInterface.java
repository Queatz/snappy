package com.queatz.snappy.logic.interfaces;

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

        // Use graph
        if (as.getParameters().containsKey(Config.PARAM_SELECT)) {
            String select = as.getParameters().get(Config.PARAM_SELECT)[0];

            FrozenQuery query = as.s(EarthQueries.class).byGoogleUrl(personName);

            return as.s(EarthJson.class).toJson(
                    as.s(EarthGraph.class).queryOne(query.getEarthQuery(), select, query.getVars())
            );
        }

        EarthThing person = as.s(PersonMine.class).byGoogleUrl(personName.toLowerCase());

        if (person == null) {
            throw new NothingLogicResponse("by name - nobody");
        }

        return as.s(EarthViewer.class).getViewForEntityOrThrow(person).toJson();
    }
}
