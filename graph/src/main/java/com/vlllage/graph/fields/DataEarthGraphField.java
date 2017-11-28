package com.vlllage.graph.fields;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.queatz.earth.EarthField;
import com.queatz.earth.EarthKind;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.shared.EarthJson;

/**
 * Created by jacob on 11/27/17.
 */

public class DataEarthGraphField extends PrimitiveEarthGraphField {
    @Override
    public String[] selection() {
        return new String[] { EarthField.DATA, EarthField.SOURCE, EarthField.KIND };
    }

    @Override
    public JsonElement view(EarthThing as, JsonElement[] selection) {
        if (EarthKind.ACTION_KIND.equals(selection[2].getAsString())) {
            if (selection[0].isJsonNull()) {
                return null;
            }

            if (as == null || !selection[1].isJsonPrimitive() || !as.key().name().equals(selection[1].getAsString())) {
                JsonObject data = EarthJson.getDefault().fromJson(selection[0].getAsString(), JsonObject.class);
                JsonObject json = new JsonObject();
                json.add("config", data.get("config"));

                return json;
            }

            return EarthJson.getDefault().fromJson(selection[0].getAsString(), JsonObject.class);
        }

        return super.view(as, selection);
    }
}
