package com.queatz.snappy.shared.things;

import com.google.appengine.api.datastore.GeoPt;
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
 * Created by jacob on 10/12/15.
 */

@Entity
public class PersonSpec extends ThingSpec {
    public @Push String firstName;
    public String lastName;
    public String imageUrl;

    public @Shallow String about;

    public @Hide @Index String token;
    public @Hide String subscription;
    public @Hide @Search("geo") GeoPt latlng;
    public @Hide String googleId;
    public @Hide @Index String googleUrl;
    public @Hide String gender;
    public @Hide String language;
    public @Hide @Index String email;
    public @Hide @Index @Search("age") Date around;

    public @Ignore String auth;
    public @Ignore @Shallow int infoFollowers;
    public @Ignore @Shallow int infoFollowing;
    public @Ignore @Shallow int infoHosted;
    public @Ignore @Shallow boolean infoAvailability;
    public @Ignore @Shallow double infoDistance;
    public @Ignore @Shallow List<FollowLinkSpec> followers;
    public @Ignore @Shallow List<UpdateSpec> updates;
    public @Ignore @Shallow List<OfferSpec> offers;
}