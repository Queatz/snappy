package com.queatz.snappy.logic.special;

import com.google.appengine.api.datastore.GeoPt;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthJson;
import com.queatz.snappy.logic.EarthKind;
import com.queatz.snappy.logic.EarthSingleton;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.snappy.logic.concepts.Interfaceable;
import com.queatz.snappy.logic.exceptions.NothingLogicResponse;
import com.queatz.snappy.logic.view.EnitityListView;

import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

/**
 * Created by jacob on 4/9/16.
 */
public class HereInterface implements Interfaceable {

    private EarthStore earthStore = EarthSingleton.of(EarthStore.class);

    @Override
    public String get(@Nonnull List<String> route, @Nonnull Map<String, String[]> parameters) {
        if (!parameters.containsKey(EarthField.LATITUDE)
                || !parameters.containsKey(EarthField.LONGITUDE)) {
            throw new NothingLogicResponse("here - no latitude and longitude");
        }

        String latitudeParam = parameters.get(EarthField.LATITUDE)[0];
        String longitudeParam = parameters.get(EarthField.LONGITUDE)[0];

        float latitude = Float.valueOf(latitudeParam);
        float longitude = Float.valueOf(longitudeParam);

        String kindFilter = null;

        if (route.size() > 1) {
            kindFilter = route.get(1);
        }

        // TODO - and make them views
        return new EnitityListView(earthStore.queryNearTo(new GeoPt(latitude, longitude), kindFilter)).toJson();
    }

    @Override
    public String post(@Nonnull List<String> route, @Nonnull Map<String, String[]> parameters) {
        return null;
    }
}
