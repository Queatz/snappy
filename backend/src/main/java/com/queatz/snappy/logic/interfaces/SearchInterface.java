package com.queatz.snappy.logic.interfaces;

import com.queatz.earth.EarthQueries;
import com.queatz.earth.FrozenQuery;
import com.queatz.snappy.as.EarthAs;
import com.queatz.snappy.exceptions.NothingLogicResponse;
import com.queatz.snappy.router.Interfaceable;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.shared.EarthJson;
import com.queatz.snappy.shared.earth.EarthGeo;
import com.vlllage.graph.EarthGraph;

/**
 * Created by jacob on 7/9/16.
 */
public class SearchInterface implements Interfaceable {

    @Override
    public String get(EarthAs as) {
        if (!as.getParameters().containsKey(Config.PARAM_LATITUDE)
                || !as.getParameters().containsKey(Config.PARAM_LONGITUDE)) {
            throw new NothingLogicResponse("search - no latitude and longitude");
        }

        String latitudeParam = as.getParameters().get(Config.PARAM_LATITUDE)[0];
        String longitudeParam = as.getParameters().get(Config.PARAM_LONGITUDE)[0];

        String qParam;

        if (as.getParameters().containsKey(Config.PARAM_Q)) {
            qParam = as.getParameters().get(Config.PARAM_Q)[0];
        } else {
            qParam = null;
        }

        float latitude = Float.valueOf(latitudeParam);
        float longitude = Float.valueOf(longitudeParam);

        String kindFilter = null;

        if (as.getRoute().size() > 1) {
            kindFilter = as.getRoute().get(1);
        }

        final EarthGeo latLng = EarthGeo.of(latitude, longitude);

        String select = as.getParameters().containsKey(Config.PARAM_SELECT) ? as.getParameters().get(Config.PARAM_SELECT)[0] : null;
        FrozenQuery query = as.s(EarthQueries.class).getNearby(latLng, kindFilter, qParam);

        return as.s(EarthJson.class).toJson(
                as.s(EarthGraph.class).query(query.getEarthQuery(), select, query.getVars())
        );
    }

    @Override
    public String post(EarthAs as) {
        return null;
    }
}
