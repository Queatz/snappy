package com.queatz.snappy.logic.concepts;

import com.google.cloud.datastore.Entity;
import com.queatz.snappy.logic.EarthRule;

/**
 * Can I see you?
 */
public interface Authority {
    boolean authorize(Entity as, Entity entity, EarthRule rule);
}
