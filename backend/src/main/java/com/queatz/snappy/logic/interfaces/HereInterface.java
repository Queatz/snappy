package com.queatz.snappy.logic.interfaces;

import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthGeo;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.snappy.logic.EarthView;
import com.queatz.snappy.logic.concepts.Interfaceable;
import com.queatz.snappy.logic.editors.PersonEditor;
import com.queatz.snappy.logic.exceptions.NothingLogicResponse;
import com.queatz.snappy.logic.views.EntityListView;
import com.queatz.snappy.shared.Config;

/**
 * Created by jacob on 4/9/16.
 */
public class HereInterface implements Interfaceable {

    @Override
    public String get(EarthAs as) {
        if (!as.getParameters().containsKey(Config.PARAM_LATITUDE)
                || !as.getParameters().containsKey(Config.PARAM_LONGITUDE)) {
            throw new NothingLogicResponse("here - no latitude and longitude");
        }

        String latitudeParam = as.getParameters().get(Config.PARAM_LATITUDE)[0];
        String longitudeParam = as.getParameters().get(Config.PARAM_LONGITUDE)[0];
        double latitude = Float.valueOf(latitudeParam);
        double longitude = Float.valueOf(longitudeParam);
        final EarthGeo latLng = EarthGeo.of(latitude, longitude);

        if (as.hasUser()) {
            new PersonEditor(as).updateLocation(as.getUser(), latLng);
        }

        boolean recent = false;

        if (as.getParameters().containsKey(Config.PARAM_RECENT)) {
            recent = Boolean.valueOf(as.getParameters().get(Config.PARAM_RECENT)[0]);
        }

        String kindFilter = null;

        if (as.getRoute().size() > 1) {
            kindFilter = as.getRoute().get(1);
        }

        return new EntityListView(as, new EarthStore(as)
                .getNearby(latLng, kindFilter, recent, null), EarthView.SHALLOW).toJson();
    }

    @Override
    public String post(EarthAs as) {
        return null;
    }
}
