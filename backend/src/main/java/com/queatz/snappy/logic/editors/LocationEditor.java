package com.queatz.snappy.logic.editors;

import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.LatLng;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthKind;
import com.queatz.snappy.logic.EarthSingleton;
import com.queatz.snappy.logic.EarthStore;

/**
 * Created by jacob on 5/14/16.
 */
public class LocationEditor {

    EarthStore earthStore = EarthSingleton.of(EarthStore.class);

    public Entity newLocation(String name, String address, LatLng latLng) {
        return earthStore.save(earthStore.edit(earthStore.create(EarthKind.LOCATION_KIND))
            .set(EarthField.NAME, name)
            .set(EarthField.ADDRESS, address)
            .set(EarthField.GEO, latLng));
    }
}
