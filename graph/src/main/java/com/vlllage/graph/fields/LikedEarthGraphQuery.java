package com.vlllage.graph.fields;

import com.queatz.earth.EarthField;
import com.queatz.earth.EarthKind;
import com.queatz.earth.EarthQuery;
import com.queatz.snappy.as.EarthAs;

import org.jetbrains.annotations.Nullable;

/**
 * Created by jacob on 12/3/17.
 */

public class LikedEarthGraphQuery extends BooleanQueryEarthGraphField {
    @Nullable
    @Override
    public EarthQuery query(EarthAs as) {
        if (!as.hasUser()) {
            return null;
        }

        return new EarthQuery(as).filter("{thing}." + EarthField.KIND + " == '" + EarthKind.LIKE_KIND + "' and " +
                "{thing}." + EarthField.SOURCE + " == '" + as.getUser().key().name() + "' and " +
                "{thing}." + EarthField.TARGET + " == {parent}._key")
                .select("true")
                .single()
                .limit("1");
    }
}
