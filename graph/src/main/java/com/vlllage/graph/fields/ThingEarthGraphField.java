package com.vlllage.graph.fields;

import com.google.gson.JsonElement;
import com.queatz.earth.EarthThing;

/**
 * Created by jacob on 11/26/17.
 */

public abstract class ThingEarthGraphField implements EarthGraphField {
    @Override
    public boolean isQuery() {
        return true;
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public String[] selection() {
        return new String[0];
    }

    @Override
    public JsonElement view(EarthThing as, JsonElement[] selection) {
        return null;
    }
}
