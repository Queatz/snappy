package com.vlllage.graph.fields;

import com.queatz.earth.EarthField;
import com.queatz.earth.EarthQuery;
import com.queatz.snappy.as.EarthAs;

/**
 * Created by jacob on 11/26/17.
 */

public class SourceEarthGraphField extends ThingEarthGraphField {
    @Override
    public EarthQuery query(EarthAs as) {
        return new EarthQuery(as)
                .filter("_key", "{parent}." + EarthField.SOURCE)
                .limit("1");
    }
}
