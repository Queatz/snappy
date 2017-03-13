package com.queatz.snappy.logic.concepts;

import com.queatz.snappy.logic.EarthRule;
import com.queatz.snappy.logic.EarthThing;

/**
 * Authorities allow kinds of things to allow or deny different kinds of access to the requesting
 * user based on custom rules.
 */
public interface Authority {
    /**
     * A function to determine user authorization to a thing.
     *
     * @param as The user requesting access
     * @param entity The entity in question
     * @param rule The rule in question
     * @return Whether or not to grant access to this user
     */
    boolean authorize(EarthThing as, EarthThing entity, EarthRule rule);
}
