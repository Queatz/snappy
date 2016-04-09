package com.queatz.snappy.logic.concepts;

import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

/**
 * Created by jacob on 4/1/16.
 */

public interface Interfaceable {

    /**
     * Fulfills a GET request for the end user.
     *
     * - This will never modify entities.
     *
     * @param route The path the user requested.
     * @param parameters The user-supplied parameters.
     * @return A JSON response as a string.
     */
    String get(@Nonnull List<String> route, @Nonnull Map<String, String[]> parameters);

    /**
     * Fulfills a POST request for the end user. Use this for all actions taken.
     *
     * - This might modify entities.
     * - Entity kind can be inferred from parameters or an existing id.
     *
     * @param route The path the user requested.
     * @param parameters The user-supplied parameters.
     * @return A JSON response as a string.
     */
    String post(@Nonnull List<String> route, @Nonnull Map<String, String[]> parameters);
}
