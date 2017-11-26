package com.vlllage.graph.fields;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.queatz.earth.EarthField;
import com.queatz.earth.EarthQuery;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.as.EarthAs;
import com.queatz.snappy.shared.Shared;
import com.queatz.snappy.shared.earth.EarthGeo;

/**
 * Created by jacob on 11/27/17.
 */

public class InfoDistanceEarthGraphField implements EarthGraphField {
    @Override
    public boolean isQuery() {
        return false;
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public EarthQuery query(EarthAs as) {
        return null;
    }

    @Override
    public String[] selection() {
        return new String[] {EarthField.GEO};
    }

    @Override
    public JsonElement view(EarthThing as, JsonElement[] selection) {
        if (as == null || !as.has(EarthField.GEO) || !selection[0].isJsonArray()) {
            return null;
        }

        return new JsonPrimitive(
                Shared.distance(as.getGeo(EarthField.GEO), EarthGeo.of(
                        ((JsonArray) selection[0]).get(0).getAsDouble(),
                        ((JsonArray) selection[0]).get(1).getAsDouble()
                ))
        );
    }
}
