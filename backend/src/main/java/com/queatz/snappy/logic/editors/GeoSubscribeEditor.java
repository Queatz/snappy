package com.queatz.snappy.logic.editors;

import com.queatz.earth.EarthField;
import com.queatz.earth.EarthKind;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.api.EarthAs;
import com.queatz.snappy.api.EarthControl;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.snappy.shared.Shared;
import com.queatz.snappy.shared.earth.EarthGeo;

import org.jetbrains.annotations.NotNull;

import java.util.Date;

/**
 * Created by jacob on 5/8/17.
 */

public class GeoSubscribeEditor extends EarthControl {

    public GeoSubscribeEditor(@NotNull EarthAs as) {
        super(as);
    }

    public EarthThing create(double latitude, double longitude, String email, String locality) {
        EarthThing geoSubscribe = use(EarthStore.class).create(EarthKind.GEO_SUBSCRIBE_KIND);
        EarthThing.Builder edit = use(EarthStore.class).edit(geoSubscribe)
                .set(EarthField.GEO, EarthGeo.of(latitude, longitude))
                .set(EarthField.EMAIL, email)
                .set(EarthField.NAME, locality)
                .set(EarthField.UNSUBSCRIBE_TOKEN, Shared.randomToken())
                .set(EarthField.UPDATED_ON, new Date())
                .set(EarthField.CREATED_ON, new Date());

        return use(EarthStore.class).save(edit);
    }
}
