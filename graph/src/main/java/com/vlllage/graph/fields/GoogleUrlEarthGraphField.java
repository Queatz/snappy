package com.vlllage.graph.fields;

import com.queatz.earth.EarthField;

/**
 * Created by jacob on 11/27/17.
 */

public class GoogleUrlEarthGraphField extends PrimitiveEarthGraphField {
    @Override
    public String[] selection() {
        return new String[] { EarthField.GOOGLE_URL };
    }
}