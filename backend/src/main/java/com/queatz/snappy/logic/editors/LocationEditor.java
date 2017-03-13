package com.queatz.snappy.logic.editors;

import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthControl;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthGeo;
import com.queatz.snappy.logic.EarthKind;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.snappy.logic.EarthThing;

/**
 * Created by jacob on 5/14/16.
 */
public class LocationEditor extends EarthControl {
    private final EarthStore earthStore;

    public LocationEditor(final EarthAs as) {
        super(as);

        earthStore = use(EarthStore.class);
    }

    public EarthThing newLocation(String name, String address, EarthGeo latLng) {
        return earthStore.save(earthStore.edit(earthStore.create(EarthKind.LOCATION_KIND))
            .set(EarthField.NAME, name)
            .set(EarthField.ADDRESS, address)
            .set(EarthField.GEO, latLng));
    }
}
