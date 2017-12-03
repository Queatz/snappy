package com.vlllage.graph.fields;

import com.google.gson.JsonElement;
import com.queatz.earth.EarthField;
import com.queatz.earth.EarthKind;
import com.queatz.earth.EarthQuery;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.as.EarthAs;

/**
 * Created by jacob on 12/3/17.
 */

public class LikersEarthGraphField implements EarthGraphField {
    @Override
    public Type type() {
        return Type.EXPRESSION;
    }

    @Override
    public EarthQuery query(EarthAs as) {
        return new EarthQuery(as).filter("{thing}." + EarthField.KIND + " == '" + EarthKind.LIKE_KIND + "' and " +
                "{thing}." + EarthField.TARGET + " == {parent}._key")
                .inline().count(true);
    }

    @Override
    public String[] selection() {
        return new String[0];
    }

    @Override
    public JsonElement view(EarthThing as, JsonElement[] selection) {
        return selection[0];
    }
}
