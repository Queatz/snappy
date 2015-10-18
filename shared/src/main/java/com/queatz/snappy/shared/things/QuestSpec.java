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
import java.util.List;

/**
 * Created by jacob on 10/14/15.
 */

@Entity
public class QuestSpec extends ThingSpec {
    public @Push String name;
    public String details;
    public String reward;
    public @Search("status") @Index String status;
    public String time;
    public @Index Date opened;
    public int teamSize;
    public @Search("geo") @Hide GeoPt latlng;

    public @Hide @Index Key<PersonSpec> hostId;

    public @Ignore @Push List<PersonSpec> team;
    public @Ignore PersonSpec host;
}