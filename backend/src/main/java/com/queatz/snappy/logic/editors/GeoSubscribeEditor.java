package com.queatz.snappy.logic.editors;

import com.queatz.snappy.backend.Util;
import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthControl;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.shared.earth.EarthGeo;
import com.queatz.snappy.logic.EarthKind;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.snappy.logic.EarthThing;

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
                .set(EarthField.UNSUBSCRIBE_TOKEN, Util.randomToken())
                .set(EarthField.UPDATED_ON, new Date())
                .set(EarthField.CREATED_ON, new Date());

        return use(EarthStore.class).save(edit);
    }
}
