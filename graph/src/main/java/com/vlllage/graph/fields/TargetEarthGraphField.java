package com.vlllage.graph.fields;

import com.queatz.earth.EarthField;

/**
 * Created by jacob on 11/27/17.
 */

public class TargetEarthGraphField extends ThingEarthGraphField {
    @Override
    protected String field() {
        return EarthField.TARGET;
    }
}
