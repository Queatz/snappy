package com.vlllage.graph.fields;

import com.google.gson.JsonElement;
import com.queatz.earth.EarthField;
import com.queatz.earth.EarthThing;

/**
 * Created by jacob on 11/27/17.
 */

public class AuthEarthGraphField extends PrimitiveEarthGraphField {
    @Override
    public String[] selection() {
        return new String[] { EarthField.TOKEN };
    }

    @Override
    public JsonElement view(EarthThing as, JsonElement[] selection) {
        if (as == null || !as.key().name().equals(selection[0].getAsString())) {
            return null;
        }

        return super.view(as, selection);
    }
}
