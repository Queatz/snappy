package com.vlllage.graph.fields;

import com.google.gson.JsonElement;
import com.queatz.earth.EarthQuery;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.as.EarthAs;

/**
 * Created by jacob on 11/26/17.
 */

public abstract class PrimitiveEarthGraphField implements EarthGraphField {
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
    public JsonElement view(EarthThing as, JsonElement[] selection) {
        return selection[0];
    }
}
