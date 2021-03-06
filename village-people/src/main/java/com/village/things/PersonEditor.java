package com.village.things;

import com.queatz.earth.EarthField;
import com.queatz.earth.EarthKind;
import com.queatz.earth.EarthStore;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.as.EarthAs;
import com.queatz.snappy.as.EarthControl;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.shared.Shared;
import com.queatz.snappy.shared.earth.EarthGeo;
import com.queatz.snappy.shared.earth.EarthRef;

import org.apache.commons.lang3.StringUtils;

import java.util.Date;

/**
 * Created by jacob on 5/8/16.
 */
public class PersonEditor extends EarthControl {
    private final EarthStore earthStore;

    public PersonEditor(final EarthAs as) {
        super(as);

        earthStore = use(EarthStore.class);
    }


    public EarthThing newPerson() {
        return earthStore.save(earthStore.edit(earthStore.create(EarthKind.PERSON_KIND))
                .set(EarthField.TOKEN, Shared.genToken())
                .set(EarthField.ABOUT)
                .set(EarthField.SUBSCRIPTION));
    }

    public EarthThing newPerson(String email) {
        EarthThing person = earthStore.save(earthStore.edit(earthStore.create(EarthKind.PERSON_KIND))
                .set(EarthField.TOKEN, Shared.genToken())
                .set(EarthField.EMAIL, email)
                .set(EarthField.ABOUT)
                .set(EarthField.SUBSCRIPTION));

        // A person starts out as their own club
        earthStore.addToClub(person, person);
        earthStore.setOwner(person, person);

        return person;
    }

    public EarthThing updatePerson(EarthThing person,
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
        EarthThing.Builder update = earthStore.edit(person)
                .set(EarthField.FIRST_NAME, nullableString(firstName))
                .set(EarthField.LAST_NAME, nullableString(lastName))
                .set(EarthField.GENDER, nullableString(gender))
                .set(EarthField.LANGUAGE, nullableString(language))
                .set(EarthField.IMAGE_URL, nullableString(imageUrl));

        if (StringUtils.isBlank(person.getString(EarthField.TOKEN))) {
            update.set(EarthField.TOKEN, token);
        }

        if (StringUtils.isBlank(person.getString(EarthField.ABOUT))) {
            update.set(EarthField.ABOUT, nullableString(about));
        }

        if (StringUtils.isBlank(person.getString(EarthField.GOOGLE_URL))) {
            if (!StringUtils.isBlank(googleUrl)) {
                update.set(EarthField.GOOGLE_URL, googleUrl);
            } else {
                update.set(EarthField.GOOGLE_URL, googleId);
            }
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

    public void updateSubscription(EarthThing person, String subscription) {
        earthStore.save(earthStore.edit(person).set(EarthField.SUBSCRIPTION, subscription));
    }

    public void updateLocation(EarthThing person, EarthGeo latLng) {
        earthStore.save(earthStore.edit(person)
                .set(EarthField.GEO, latLng)
                .set(EarthField.AROUND, new Date()));
    }

    public void updateAbout(EarthThing person, String about) {
        earthStore.save(earthStore.edit(person).set(EarthField.ABOUT, about));
    }

    public boolean updateLink(EarthThing person, String link) {
        if (new PersonMine(new EarthAs()).byGoogleUrl(link) != null) {
            return false;
        }

        earthStore.save(earthStore.edit(person).set(EarthField.GOOGLE_URL, link));
        return true;
    }

    public EarthThing updateCover(EarthThing person, String coverId) {
        return earthStore.save(earthStore.edit(person).set(EarthField.COVER, EarthRef.of(coverId)));
    }
}
