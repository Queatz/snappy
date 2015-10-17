package com.queatz.snappy.shared.things;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Ignore;
import com.queatz.snappy.shared.Hide;
import com.queatz.snappy.shared.ThingSpec;

import java.util.Date;

/**
 * Created by jacob on 10/14/15.
 */

@Entity
public class ContactSpec extends ThingSpec {
    public Date updated;
    public boolean seen;

    public @Hide Key<PersonSpec> personId;
    public @Hide Key<PersonSpec> contactId;
    public @Hide Key<MessageSpec> lastId;

    public @Ignore PersonSpec person;
    public @Ignore PersonSpec contact;
    public @Ignore MessageSpec last;
}
