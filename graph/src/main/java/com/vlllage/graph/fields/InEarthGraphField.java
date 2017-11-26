package com.vlllage.graph.fields;

import com.queatz.earth.EarthField;
import com.queatz.earth.EarthKind;
import com.queatz.earth.EarthQuery;
import com.queatz.snappy.as.EarthAs;
import com.queatz.snappy.shared.Config;

/**
 * Created by jacob on 11/27/17.
 */

public class InEarthGraphField extends EarthThingListGraphField {
    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public EarthQuery query(EarthAs as) {
        return new EarthQuery(as)
                .filter("{thing}." + EarthField.KIND + " == '" + EarthKind.MEMBER_KIND + "' and " +
                        "{thing}." + EarthField.SOURCE + " == {parent}._key and " +
                        "{thing}." + EarthField.STATUS + " == '" + Config.MEMBER_STATUS_ACTIVE + "'");
    }
}
