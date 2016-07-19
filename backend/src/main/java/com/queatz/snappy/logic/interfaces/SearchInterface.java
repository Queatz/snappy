package com.queatz.snappy.logic.interfaces;

import com.google.cloud.datastore.LatLng;
import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.snappy.logic.EarthView;
import com.queatz.snappy.logic.concepts.Interfaceable;
import com.queatz.snappy.logic.exceptions.NothingLogicResponse;
import com.queatz.snappy.logic.views.EntityListView;
import com.queatz.snappy.shared.Config;

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

        String qParam = as.getParameters().get(Config.PARAM_Q)[0];
        float latitude = Float.valueOf(latitudeParam);
        float longitude = Float.valueOf(longitudeParam);

        String kindFilter = null;

        if (as.getRoute().size() > 1) {
            kindFilter = as.getRoute().get(1);
        }

        final LatLng latLng = LatLng.of(latitude, longitude);

        return new EntityListView(as,
                new EarthStore(as).getNearby(latLng, kindFilter, qParam), EarthView.SHALLOW).toJson();
    }

    @Override
    public String post(EarthAs as) {
        return null;
    }
}
