package com.queatz.snappy.logic.concepts;

import com.google.cloud.datastore.Entity;
import com.queatz.snappy.logic.EarthRule;

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
    boolean authorize(Entity as, Entity entity, EarthRule rule);
}
