package com.vlllage.graph.fields;

import com.queatz.earth.EarthField;
import com.queatz.earth.EarthKind;
import com.queatz.earth.EarthQuery;
import com.queatz.earth.EarthStore;
import com.queatz.snappy.as.EarthAs;

import static com.queatz.earth.EarthStore.CLUB_GRAPH;

/**
 * Created by jacob on 11/27/17.
 */

public class ClubsEarthGraphField extends EarthThingListGraphField {
    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public EarthQuery query(EarthAs as) {
        return new EarthQuery(as)
                .in("outbound {parent}._id graph '" + CLUB_GRAPH + "'")
                .filter(EarthField.KIND, "'" + EarthKind.CLUB_KIND + "'")
                .filter(EarthStore.DEFAULT_FIELD_TO, "!=", "{parent}._id")
                .distinct(true)
                .sort("{thing}." + EarthField.CREATED_ON + " desc");
    }
}
