package com.vlllage.graph.fields;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.queatz.earth.EarthField;
import com.queatz.earth.EarthQuery;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.as.EarthAs;

/**
 * Created by jacob on 11/6/17.
 */

public class OwnerEarthGraphField implements EarthGraphField {
    @Override
    public Type type() {
        return Type.VALUE;
    }

    @Override
    public EarthQuery query(EarthAs as) {
        return null;
    }

    @Override
    public String[] selection() {
        return new String[] { "_key", EarthField.SOURCE };
    }

    @Override
    public JsonElement view(EarthThing as, JsonElement[] selection) {
        return new JsonPrimitive(as != null &&
                (selection[0].getAsString().equals(as.key().name()) ||
                        (!selection[1].isJsonNull() && selection[1].getAsString().equals(as.key().name()))));
    }
}
