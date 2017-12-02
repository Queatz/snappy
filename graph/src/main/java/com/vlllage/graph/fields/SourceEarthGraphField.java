package com.vlllage.graph.fields;

import com.queatz.earth.EarthField;

/**
 * Created by jacob on 11/26/17.
 */

public class SourceEarthGraphField extends ThingEarthGraphField {
    @Override
    protected String field() {
        return EarthField.SOURCE;
    }
}
