package com.vlllage.graph.fields;

import com.queatz.earth.EarthField;

/**
 * Created by jacob on 12/2/17.
 */

public class CoverEarthGraphField extends ThingEarthGraphField {
    @Override
    protected String field() {
        return EarthField.COVER;
    }
}
