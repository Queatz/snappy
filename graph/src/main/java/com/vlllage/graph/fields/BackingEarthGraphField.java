package com.vlllage.graph.fields;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.queatz.earth.EarthField;
import com.queatz.earth.EarthKind;
import com.queatz.earth.EarthQuery;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.as.EarthAs;

/**
 * Created by jacob on 11/28/17.
 */

public class BackingEarthGraphField implements EarthGraphField {
    @Override
    public Type type() {
        return Type.EXPRESSION;
    }

    @Override
    public EarthQuery query(EarthAs as) {
        if (!as.hasUser()) {
            return null;
        }

        return new EarthQuery(as).filter("{thing}." + EarthField.KIND + " == '" + EarthKind.FOLLOWER_KIND + "' and " +
                "{thing}." + EarthField.SOURCE + " == '" + as.getUser().key().name() + "' and " +
                "{thing}." + EarthField.TARGET + " == {parent}._key")
                .select("true")
                .single()
                .limit("1");
    }

    @Override
    public String[] selection() {
        return null;
    }

    @Override
    public JsonElement view(EarthThing as, JsonElement[] selection) {
        return new JsonPrimitive(selection[0].isJsonPrimitive() && selection[0].getAsBoolean());
    }
}
