package com.vlllage.graph.fields;

import com.queatz.earth.EarthField;
import com.queatz.earth.EarthKind;
import com.queatz.earth.EarthQuery;
import com.queatz.snappy.as.EarthAs;
import com.queatz.snappy.shared.Config;

/**
 * Created by jacob on 11/25/17.
 */

public class MembersEarthGraphField extends EarthThingListGraphField {

    @Override
    public EarthQuery query(EarthAs as) {
        return new EarthQuery(as)
                .filter("{thing}." + EarthField.KIND + " == '" + EarthKind.MEMBER_KIND + "' and " +
                        "{thing}." + EarthField.TARGET + " == {parent}._key and " +
                        "{thing}." + EarthField.STATUS + " == '" + Config.MEMBER_STATUS_ACTIVE + "'");
    }
}