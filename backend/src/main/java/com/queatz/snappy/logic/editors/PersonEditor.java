package com.queatz.snappy.logic.editors;

import com.google.cloud.datastore.DateTime;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.LatLng;
import com.google.cloud.datastore.NullValue;
import com.queatz.snappy.backend.Util;
import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthControl;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthKind;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.snappy.shared.Config;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by jacob on 5/8/16.
 */
public class PersonEditor extends EarthControl {
    private final EarthStore earthStore;

    public PersonEditor(final EarthAs as) {
        super(as);

        earthStore = use(EarthStore.class);
    }


    public Entity newPerson() {
        return earthStore.save(earthStore.edit(earthStore.create(EarthKind.PERSON_KIND))
                .set(EarthField.TOKEN, Util.genToken())
                .set(EarthField.ABOUT, NullValue.of())
                .set(EarthField.SUBSCRIPTION, NullValue.of()));
    }

    public Entity newPerson(String email) {
        return earthStore.save(earthStore.edit(earthStore.create(EarthKind.PERSON_KIND))
                .set(EarthField.TOKEN, Util.genToken())
                .set(EarthField.EMAIL, email)
                .set(EarthField.ABOUT, NullValue.of())
                .set(EarthField.SUBSCRIPTION, NullValue.of()));
    }

    public Entity updatePerson(Entity person,
                               String token,
                               String firstName,
                               String lastName,
                               String gender,
                               String language,
                               String imageUrl,
                               String googleId,
                               String googleUrl,
                               String about,
                               String subscription) {
        Entity.Builder update = earthStore.edit(person)
                .set(EarthField.FIRST_NAME, nullableString(firstName))
                .set(EarthField.LAST_NAME, nullableString(lastName))
                .set(EarthField.GENDER, nullableString(gender))
                .set(EarthField.LANGUAGE, nullableString(language))
                .set(EarthField.IMAGE_URL, nullableString(imageUrl))
                .set(EarthField.GOOGLE_ID, nullableString(googleId))
                .set(EarthField.GOOGLE_URL, nullableString(googleUrl));

        if (StringUtils.isBlank(person.getString(EarthField.TOKEN))) {
            update.set(EarthField.TOKEN, token);
        }

        if (StringUtils.isBlank(person.getString(EarthField.ABOUT))) {
            update.set(EarthField.ABOUT, about == null ? "" : about);
        }

        if (StringUtils.isBlank(googleUrl)) {
            update.set(EarthField.GOOGLE_URL, googleId);
        }

        if (!StringUtils.isBlank(subscription)) {
            update.set(EarthField.SUBSCRIPTION, subscription);
        } else if (StringUtils.isBlank(person.getString(EarthField.SUBSCRIPTION))) {
            if (Config.IN_BETA) {
                update.set(EarthField.SUBSCRIPTION, Config.HOSTING_BETATESTER);
            } else if (Config.PUBLIC_BUY) {
                update.set(EarthField.SUBSCRIPTION, Config.HOSTING_ENABLED_AVAILABLE);
            }
        }

        return earthStore.save(update);
    }

    public String nullableString(String string) {
        if (string == null) {
            return "";
        }

        return string;
    }

    public void updateSubscription(Entity person, String subscription) {
        earthStore.save(earthStore.edit(person).set(EarthField.SUBSCRIPTION, subscription));
    }

    public void updateLocation(Entity person, LatLng latLng) {
        earthStore.save(earthStore.edit(person)
                .set(EarthField.GEO, latLng)
                .set(EarthField.AROUND, DateTime.now()));
    }

    public void updateAbout(Entity person, String about) {
        earthStore.save(earthStore.edit(person).set(EarthField.ABOUT, about));
    }
}
