package com.queatz.snappy.shared.things;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Ignore;
import com.queatz.snappy.shared.Hide;
import com.queatz.snappy.shared.Push;
import com.queatz.snappy.shared.ThingSpec;

import java.util.Date;

/**
 * Created by jacob on 10/12/15.
 */

@Entity
public class UpdateSpec extends ThingSpec {
    public String action;
    public Date date;
    public String message;

    public @Hide @Push Key<PersonSpec> personId;
    public @Hide Key<PartySpec> partyId;

    public @Ignore @Push PersonSpec person;
    public @Ignore PartySpec party;
}
