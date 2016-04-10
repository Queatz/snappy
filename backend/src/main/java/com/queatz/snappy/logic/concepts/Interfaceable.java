package com.queatz.snappy.logic.concepts;

import com.queatz.snappy.logic.EarthAs;

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
     *
     * @param as@return A JSON response as a string.
     */
    String get(EarthAs as);

    /**
     * Fulfills a POST request for the end user. Use this for all actions taken.
     *
     * - This might modify entities.
     * - Entity kind can be inferred from parameters or an existing id.
     *
     *
     * @param as@return A JSON response as a string.
     */
    String post(EarthAs as);
}
