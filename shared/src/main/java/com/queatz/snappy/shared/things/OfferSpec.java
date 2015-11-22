package com.queatz.snappy.shared.things;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.Index;
import com.queatz.snappy.shared.Hide;
import com.queatz.snappy.shared.Shallow;
import com.queatz.snappy.shared.ThingSpec;

import java.util.Date;

/**
 * Created by jacob on 10/14/15.
 */

@Entity
public class OfferSpec extends ThingSpec {
    public int price;
    public String details;
    public String unit;
    public Date created;
    public Date stopped;

    public @Index @Hide Key<PersonSpec> personId;

    public @Ignore PersonSpec person;
}
