package com.queatz.snappy.shared.things;

import com.google.appengine.api.datastore.GeoPt;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Ignore;
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

    public @Hide String token;
    public @Hide String subscription;
    public @Hide @Search("geo") GeoPt latlng;
    public @Hide String googleId;
    public @Hide String gender;
    public @Hide String email;
    public @Hide @Search("age") Date around;

    public @Ignore String auth;
    public @Ignore @Shallow int infoFollowers;
    public @Ignore @Shallow int infoFolowing;
    public @Ignore @Shallow int infoHosted;
    public @Ignore @Shallow List<FollowLinkSpec> followers;
    public @Ignore @Shallow List<UpdateSpec> updates;
    public @Ignore @Shallow List<OfferSpec> offers;
}