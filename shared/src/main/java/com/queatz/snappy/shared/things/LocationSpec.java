package com.queatz.snappy.shared.things;

import com.google.appengine.api.datastore.GeoPt;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Ignore;
import com.queatz.snappy.shared.Hide;
import com.queatz.snappy.shared.Search;
import com.queatz.snappy.shared.ThingSpec;

/**
 * Created by jacob on 10/14/15.
 */

@Entity
public class LocationSpec extends ThingSpec {
    public @Search("name") String name;
    public String address;

    public @Search("geo") @Hide GeoPt latlng;

    public @Ignore float latitude;
    public @Ignore float longitude;
}
