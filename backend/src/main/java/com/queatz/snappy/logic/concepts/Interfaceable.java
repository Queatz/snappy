package com.queatz.snappy.logic.concepts;

import com.queatz.snappy.logic.EarthAs;

/**
 * Implementations of Interfaceable expose Things to the outside world.
 */

public interface Interfaceable {

    /**
     * Fulfills a GET request for the end user.
     *
     * - This will never modify entities.
     *
     *
     * @param as
     * @return A JSON response as a string.
     */
    String get(EarthAs as);

    /**
     * Fulfills a POST request for the end user. Use this for all actions taken.
     *
     * - This might modify entities.
     * - Entity kind can be inferred from parameters or an existing id.
     *
     *
     * @param as
     * @return A JSON response as a string.
     */
    String post(EarthAs as);
}
