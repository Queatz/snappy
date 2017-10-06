package com.queatz.snappy.logic.mines;

import com.google.common.collect.ImmutableMap;
import com.queatz.snappy.as.EarthAs;
import com.queatz.snappy.as.EarthControl;
import com.queatz.earth.EarthField;
import com.queatz.earth.EarthKind;
import com.queatz.earth.EarthStore;
import com.queatz.earth.EarthThing;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Created by jacob on 5/8/17.
 */

public class GeoSubscribeMine extends EarthControl {

    public GeoSubscribeMine(@NotNull EarthAs as) {
        super(as);
    }

    public EarthThing byToken(String token) {
        List<EarthThing> geoSubscribe = use(EarthStore.class).query(
                "x." + EarthField.KIND + " == @kind and " +
                "x." + EarthField.UNSUBSCRIBE_TOKEN + " == @token",
                ImmutableMap.<String, Object>of(
                        "kind", EarthKind.GEO_SUBSCRIBE_KIND,
                        "token", token
                )
        );

        if (geoSubscribe.isEmpty()) {
            return null;
        } else {
            return geoSubscribe.get(0);
        }
    }
}
