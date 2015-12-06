package com.queatz.snappy.shared.things;

import com.google.appengine.api.datastore.GeoPt;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.Index;
import com.queatz.snappy.shared.Hide;
import com.queatz.snappy.shared.Push;
import com.queatz.snappy.shared.Search;
import com.queatz.snappy.shared.ThingSpec;

import java.util.Date;

/**
 * Created by jacob on 10/14/15.
 *
 * @deprecated See {@code OfferSpec}
 */

@Entity
public class BountySpec extends ThingSpec {
    public String details;
    public String status;
    public int price;
    public Date posted;

    public @Search("geo") @Hide GeoPt latlng;

    public @Index @Hide Key<PersonSpec> posterId;
    public @Index @Hide Key<PersonSpec> peopleId;

    public @Ignore PersonSpec poster;
    public @Ignore @Push PersonSpec people;
}
