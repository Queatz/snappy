package com.vlllage.graph.fields;

import com.queatz.earth.EarthField;

/**
 * Created by jacob on 11/27/17.
 */

public class PhotoEarthGraphField extends BooleanEarthGraphField {
    @Override
    public String[] selection() {
        return new String[] { EarthField.PHOTO };
    }
}
