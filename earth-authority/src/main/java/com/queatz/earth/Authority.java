package com.queatz.earth;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    boolean authorize(@Nullable EarthThing as, @NotNull EarthThing entity, @NotNull EarthRule rule);
}
