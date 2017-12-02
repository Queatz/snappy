package com.vlllage.graph.fields;

import com.queatz.earth.EarthField;

/**
 * Created by jacob on 11/27/17.
 */

public class LatestEarthGraphField extends ThingEarthGraphField {
    @Override
    public String field() {
        return EarthField.LATEST;
    }
}
