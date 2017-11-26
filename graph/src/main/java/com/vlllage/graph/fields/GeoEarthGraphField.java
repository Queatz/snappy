package com.vlllage.graph.fields;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.queatz.earth.EarthField;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.shared.EarthJson;
import com.queatz.snappy.shared.earth.EarthGeo;

/**
 * Created by jacob on 11/27/17.
 */

public class GeoEarthGraphField extends PrimitiveEarthGraphField {
    @Override
    public String[] selection() {
        return new String[] { EarthField.GEO };
    }

    @Override
    public JsonElement view(EarthThing as, JsonElement[] selection) {
        if (selection[0].isJsonNull()) {
            return selection[0];
        }

        return EarthJson.getDefault().toJsonTree(EarthGeo.of(
                ((JsonArray) selection[0]).get(0).getAsDouble(),
                ((JsonArray) selection[0]).get(1).getAsDouble()
        ));
    }
}
