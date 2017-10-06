package com.queatz.snappy.logic.editors;

import com.queatz.snappy.api.EarthAs;
import com.queatz.snappy.api.EarthControl;
import com.queatz.earth.EarthField;
import com.queatz.snappy.shared.earth.EarthGeo;
import com.queatz.earth.EarthKind;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.earth.EarthThing;

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
