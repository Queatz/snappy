package com.vlllage.graph.fields;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.queatz.earth.EarthThing;

/**
 * Created by jacob on 12/3/17.
 */

public abstract class BooleanQueryEarthGraphField implements EarthGraphField {
    @Override
    public Type type() {
        return Type.EXPRESSION;
    }

    @Override
    public String[] selection() {
        return null;
    }

    @Override
    public JsonElement view(EarthThing as, JsonElement[] selection) {
        return new JsonPrimitive(selection[0] != null && selection[0].isJsonPrimitive() && selection[0].getAsBoolean());
    }
}
