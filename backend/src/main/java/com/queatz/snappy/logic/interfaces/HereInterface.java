package com.queatz.snappy.logic.interfaces;

import com.google.cloud.datastore.LatLng;
import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthSingleton;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.snappy.logic.EarthView;
import com.queatz.snappy.logic.concepts.Interfaceable;
import com.queatz.snappy.logic.editors.PersonEditor;
import com.queatz.snappy.logic.exceptions.NothingLogicResponse;
import com.queatz.snappy.logic.views.EntityListView;

/**
 * Created by jacob on 4/9/16.
 */
public class HereInterface implements Interfaceable {

    private EarthStore earthStore = EarthSingleton.of(EarthStore.class);
    private PersonEditor personEditor = EarthSingleton.of(PersonEditor.class);

    @Override
    public String get(EarthAs as) {
        if (!as.getParameters().containsKey(EarthField.LATITUDE)
                || !as.getParameters().containsKey(EarthField.LONGITUDE)) {
            throw new NothingLogicResponse("here - no latitude and longitude");
        }

        String latitudeParam = as.getParameters().get(EarthField.LATITUDE)[0];
        String longitudeParam = as.getParameters().get(EarthField.LONGITUDE)[0];

        float latitude = Float.valueOf(latitudeParam);
        float longitude = Float.valueOf(longitudeParam);

        String kindFilter = null;

        if (as.getRoute().size() > 1) {
            kindFilter = as.getRoute().get(1);
        }

        final LatLng latLng = LatLng.of(latitude, longitude);

        personEditor.updateLocation(as.getUser(), latLng);

        // TODO - and make them views
        return new EntityListView(earthStore.getNearby(latLng, kindFilter), EarthView.SHALLOW).toJson();
    }

    @Override
    public String post(EarthAs as) {
        return null;
    }
}
