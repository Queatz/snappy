package com.vlllage.graph.fields;

import com.queatz.earth.EarthField;
import com.queatz.earth.EarthQuery;
import com.queatz.snappy.as.EarthAs;

/**
 * Created by jacob on 11/27/17.
 */

public class TargetEarthGraphField extends ThingEarthGraphField {
    @Override
    public EarthQuery query(EarthAs as) {
        return new EarthQuery(as)
                .filter("_key", "{parent}." + EarthField.TARGET)
                .limit("1");
    }
}
