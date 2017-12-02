package com.vlllage.graph.fields;

import com.google.gson.JsonElement;
import com.queatz.earth.EarthQuery;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.as.EarthAs;

/**
 * Created by jacob on 11/26/17.
 */

public abstract class ThingEarthGraphField implements EarthGraphField {

    protected abstract String field();

    @Override
    public Type type() {
        return Type.OBJECT;
    }

    @Override
    public String[] selection() {
        return new String[0];
    }

    @Override
    public EarthQuery query(EarthAs as) {
        return new EarthQuery(as)
                .filter("_key", "{parent}." + field())
                .limit("1");
    }

    @Override
    public JsonElement view(EarthThing as, JsonElement[] selection) {
        return null;
    }
}
