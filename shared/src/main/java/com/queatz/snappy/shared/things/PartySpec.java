package com.queatz.snappy.shared.things;

import com.google.appengine.api.datastore.GeoPt;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.Index;
import com.queatz.snappy.shared.Hide;
import com.queatz.snappy.shared.Push;
import com.queatz.snappy.shared.Search;
import com.queatz.snappy.shared.Shallow;
import com.queatz.snappy.shared.ThingSpec;

import java.util.Date;
import java.util.List;

/**
 * Created by jacob on 10/14/15.
 */

@Entity
public class PartySpec extends ThingSpec {
    public @Push String name;
    public @Push @Search("age") @Index Date date;
    public String details;
    public @Shallow boolean full;
    public @Search("geo") @Hide GeoPt latlng;

    public @Hide @Index @Push Key<PersonSpec> hostId;
    public @Hide Key<LocationSpec> locationId;
    public @Hide Key<PartySpec> originalId;

    public @Ignore @Shallow List<JoinLinkSpec> people;
    public @Ignore @Push PersonSpec host;
    public @Ignore LocationSpec location;
    public @Ignore PartySpec original;
}
