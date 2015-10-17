package com.queatz.snappy.shared;

import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Ignore;

/**
 * Created by jacob on 10/15/15.
 */
public class ThingSpec {
    public @Id String id;
    public @Ignore String localId;

    @Override
    public boolean equals(Object other) {
        return other.getClass() == getClass() && id != null && id.equals(((ThingSpec) other).id);
    }

    @Override
    public int hashCode() {
        return id == null ? 1234567890 : id.hashCode();
    }
}
