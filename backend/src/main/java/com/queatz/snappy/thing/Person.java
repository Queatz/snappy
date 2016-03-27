package com.queatz.snappy.thing;

import com.google.appengine.api.datastore.GeoPt;
import com.queatz.snappy.backend.Datastore;
import com.queatz.snappy.backend.Util;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.shared.things.PersonSpec;

import org.apache.commons.lang3.StringUtils;

import java.util.Date;

/**
 * Created by jacob on 2/15/15.
 */
public class Person {
    public boolean updateSubscription(com.queatz.snappy.shared.things.PersonSpec person, String subscriptionId) {
        if(person == null) {
            return false;
        }

        if (person.subscription == null || subscriptionId != null) {
            person.subscription = subscriptionId;
            return Datastore.save(person);
        } else {
            return false;
        }
    }

    public boolean updateAbout(com.queatz.snappy.shared.things.PersonSpec person, String about) {
        person.about = about;
        return Datastore.save(person);
    }

    public boolean updateLocation(String user, GeoPt geoPt) {
        com.queatz.snappy.shared.things.PersonSpec person = Datastore.get(com.queatz.snappy.shared.things.PersonSpec.class).id(user).now();

        if(person == null) {
            return false;
        }

        person.latlng = geoPt;
        person.around = new Date();

        return Datastore.save(person);
    }

    public com.queatz.snappy.shared.things.PersonSpec createOrUpdate(com.queatz.snappy.shared.things.PersonSpec person, com.queatz.snappy.shared.things.PersonSpec data) {
        if (person == null) {
            person = Datastore.create(com.queatz.snappy.shared.things.PersonSpec.class);
            person.token = Util.genToken();
            person.email = data.email;
        } else if (person.token == null) {
            person.token = data.token;
        }

        person.firstName = data.firstName;
        person.lastName = data.lastName;
        person.gender = data.gender;
        person.language = data.language;
        person.imageUrl = data.imageUrl;
        person.googleId = data.googleId;
        person.googleUrl = data.googleUrl;

        if (StringUtils.isBlank(person.about)) {
            person.about = data.about;
        }

        if (StringUtils.isBlank(person.googleUrl)) {
            person.googleUrl = data.googleId;
        }

        if (!StringUtils.isBlank(data.subscription)) {
            person.subscription = data.subscription;
        } else if (StringUtils.isBlank(person.subscription)) {
            if (Config.IN_BETA) {
                person.subscription = Config.HOSTING_BETATESTER;
            } else if (Config.PUBLIC_BUY) {
                person.subscription = Config.HOSTING_ENABLED_AVAILABLE;
            }
        }

        Datastore.save(person);

        return person;
    }
}