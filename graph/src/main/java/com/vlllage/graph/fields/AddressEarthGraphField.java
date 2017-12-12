package com.vlllage.graph.fields;

import com.queatz.earth.EarthField;

import org.jetbrains.annotations.Nullable;

/**
 * Created by jacob on 12/12/17.
 */

public class AddressEarthGraphField extends PrimitiveEarthGraphField {
    @Nullable
    @Override
    public String[] selection() {
        return new String[] { EarthField.ADDRESS };
    }
}
