package com.queatz.snappy.shared.things;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.Index;
import com.queatz.snappy.shared.Hide;
import com.queatz.snappy.shared.Push;
import com.queatz.snappy.shared.ThingSpec;

import java.util.Date;

/**
 * Created by jacob on 10/12/15.
 */

@Entity
public class UpdateSpec extends ThingSpec {
    public @Index String action;
    public @Index Date date;
    public String message;

    public @Index @Hide @Push Key<PersonSpec> personId;
    public @Index @Hide Key<PartySpec> partyId;

    public @Ignore @Push PersonSpec person;
    public @Ignore @Push PartySpec party;
}
