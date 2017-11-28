package com.vlllage.graph.fields;

import com.google.gson.JsonElement;
import com.queatz.earth.EarthQuery;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.as.EarthAs;

/**
 * Created by jacob on 11/6/17.
 */

public interface EarthGraphField {

    enum Type {
        LIST,
        OBJECT,
        VALUE,
        EXPRESSION
    }

    Type type();

    /**
     * @return the sub query if isQuery() returns true, otherwise null.
     * @param as
     */
    EarthQuery query(EarthAs as);

    /**
     * @return The selection if isQuery() returns false, otherwise null.
     */
    String[] selection();

    /**
     * @param as The current user
     * @param selection The queried results, from selection()
     * @return The view value
     */
    JsonElement view(EarthThing as, JsonElement[] selection);
}
