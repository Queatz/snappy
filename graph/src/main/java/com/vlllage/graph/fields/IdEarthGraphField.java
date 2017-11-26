package com.vlllage.graph.fields;

/**
 * Created by jacob on 11/6/17.
 */

public class IdEarthGraphField extends PrimitiveEarthGraphField {
    @Override
    public String[] selection() {
        return new String[] { "_key" };
    }
}
