package com.queatz.snappy.logic.mines;

import com.google.common.collect.ImmutableMap;
import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthControl;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthKind;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.snappy.logic.EarthThing;

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
                "x." + EarthField.UNSUBSCRIBE_TOKEN + " == @token and ",
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
