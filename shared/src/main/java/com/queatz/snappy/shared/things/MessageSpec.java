package com.queatz.snappy.shared.things;

import com.google.gson.annotations.Expose;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.Index;
import com.queatz.snappy.shared.Hide;
import com.queatz.snappy.shared.Push;
import com.queatz.snappy.shared.ThingSpec;

import java.util.Date;

/**
 * Created by jacob on 10/14/15.
 */

@Entity
public class MessageSpec extends ThingSpec {
    public @Index Date date;
    public @Push String message;

    public @Hide @Index Key<PersonSpec> fromId;
    public @Hide @Index Key<PersonSpec> toId;

    public @Ignore @Push PersonSpec from;
    public @Ignore PersonSpec to;
}
