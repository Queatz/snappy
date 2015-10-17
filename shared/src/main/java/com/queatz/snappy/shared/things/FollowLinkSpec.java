package com.queatz.snappy.shared.things;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Ignore;
import com.queatz.snappy.shared.Hide;
import com.queatz.snappy.shared.Push;
import com.queatz.snappy.shared.ThingSpec;

/**
 * Created by jacob on 10/14/15.
 */

@Entity
public class FollowLinkSpec extends ThingSpec {
    public @Id String id;

    public @Hide @Push Key<PersonSpec> sourceId;
    public @Hide Key<PersonSpec> targetId;

    public @Ignore @Push PersonSpec source;
    public @Ignore PersonSpec target;
}
