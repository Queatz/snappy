package com.queatz.snappy.shared.things;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.Index;
import com.queatz.snappy.shared.Hide;
import com.queatz.snappy.shared.Push;
import com.queatz.snappy.shared.ThingSpec;

/**
 * Created by jacob on 10/14/15.
 */

@Entity
public class UpdateLikeSpec extends ThingSpec {
    public @Hide @Push @Index Key<PersonSpec> personId;
    public @Hide @Push @Index Key<UpdateSpec> updateId;

    public @Ignore @Push PersonSpec person;
    public @Ignore @Push UpdateSpec update;
}
