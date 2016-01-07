package com.queatz.snappy.shared.things;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.Index;
import com.queatz.snappy.shared.Hide;
import com.queatz.snappy.shared.Push;
import com.queatz.snappy.shared.ThingSpec;

/**
 * Created by jacob on 12/26/15.
 */
@Entity
public class EndorsementSpec extends ThingSpec {
    public @Hide @Push @Index Key<PersonSpec> sourceId;
    public @Hide @Push @Index Key<OfferSpec> targetId;
    public int stars;
    public String message;

    public @Ignore @Push PersonSpec source;
    public @Ignore @Push OfferSpec target;
}
