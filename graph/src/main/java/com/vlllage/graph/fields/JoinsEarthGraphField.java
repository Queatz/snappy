package com.vlllage.graph.fields;

import com.queatz.earth.EarthField;
import com.queatz.earth.EarthKind;
import com.queatz.earth.EarthQuery;
import com.queatz.snappy.as.EarthAs;

/**
 * Created by jacob on 11/28/17.
 */

public class JoinsEarthGraphField extends EarthThingListGraphField {

    @Override
    public EarthQuery query(EarthAs as) {
        return new EarthQuery(as)
                .filter("{thing}." + EarthField.KIND + " == '" + EarthKind.JOIN_KIND + "' and " +
                        "{thing}." + EarthField.TARGET + " == {parent}._key");
    }
}
