package com.queatz.snappy.shared.things;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Ignore;
import com.queatz.snappy.shared.Hide;
import com.queatz.snappy.shared.Push;
import com.queatz.snappy.shared.ThingSpec;

import java.util.Date;

/**
 * Created by jacob on 10/14/15.
 */

@Entity
public class MessageSpec extends ThingSpec {
    public Date date;
    public @Push String message;

    public @Hide Key<PersonSpec> fromId;
    public @Hide Key<PersonSpec> toId;

    public @Ignore @Push PersonSpec from;
    public @Ignore PersonSpec to;
}
