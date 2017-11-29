package com.vlllage.graph.fields;

import com.google.gson.JsonElement;
import com.queatz.earth.EarthQuery;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.as.EarthAs;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    @NotNull
    Type type();

    /**
     * @return the sub-query.  Required for lists and objects, optional for expressions, and
     *         unused for values.
     */
    @Nullable
    EarthQuery query(EarthAs as);

    /**
     * @return The selection for values.  Not used for any other types.
     */
    @Nullable
    String[] selection();

    /**
     * Transforms values and expressions.
     *
     * @param as The current user
     * @param selection The queried results, from selection() for values, and from query()
     *                  for expressions.
     * @return The view value
     */
    JsonElement view(@Nullable EarthThing as, @NotNull JsonElement[] selection);
}
