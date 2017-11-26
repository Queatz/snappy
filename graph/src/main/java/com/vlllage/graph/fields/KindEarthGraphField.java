package com.vlllage.graph.fields;

import com.queatz.earth.EarthField;

/**
 * Created by jacob on 11/6/17.
 */

public class KindEarthGraphField extends PrimitiveEarthGraphField {
    @Override
    public String[] selection() {
        return new String[] { EarthField.KIND };
    }
}
