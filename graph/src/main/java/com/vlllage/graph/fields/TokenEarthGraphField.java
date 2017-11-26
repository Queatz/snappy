package com.vlllage.graph.fields;

import com.google.gson.JsonElement;
import com.queatz.earth.EarthField;
import com.queatz.earth.EarthThing;

/**
 * Created by jacob on 11/27/17.
 */

public class TokenEarthGraphField extends PrimitiveEarthGraphField {
    @Override
    public String[] selection() {
        return new String[] { EarthField.TOKEN, EarthField.SOURCE };
    }

    @Override
    public JsonElement view(EarthThing as, JsonElement[] selection) {
        if (selection[1].isJsonNull() || as == null || !as.key().name().equals(selection[1].getAsString())) {
            return null;
        }

        return super.view(as, selection);
    }
}
