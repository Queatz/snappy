package com.vlllage.graph.fields;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.queatz.earth.EarthThing;

/**
 * Created by jacob on 11/27/17.
 */

public abstract class BooleanEarthGraphField extends PrimitiveEarthGraphField {
    @Override
    public JsonElement view(EarthThing as, JsonElement[] selection) {
        return new JsonPrimitive(!selection[0].isJsonNull() && selection[0].getAsBoolean());

    }
}
