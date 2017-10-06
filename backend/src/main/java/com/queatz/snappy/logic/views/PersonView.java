package com.queatz.snappy.logic.views;

import com.queatz.snappy.backend.Util;
import com.queatz.snappy.logic.EarthAs;
import com.queatz.earth.EarthField;
import com.queatz.snappy.shared.earth.EarthGeo;
import com.queatz.earth.EarthKind;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.logic.EarthView;
import com.queatz.snappy.logic.mines.DeviceMine;
import com.queatz.snappy.logic.mines.FollowerMine;

import java.util.Date;

/**
 * Created by jacob on 5/8/16.
 */
public class PersonView extends CommonThingView {

    final String firstName;
    final String lastName;
    final String imageUrl;
    final String googleUrl;
    final String auth;
    final Integer infoFollowers;
    final Integer infoFollowing;
    final Integer infoOffers;
    final String socialMode;
    final Double infoDistance;
    final Date infoUpdated;
    final Date createdOn;
    final EarthGeo geo;

    public PersonView(EarthAs as, EarthThing person) {
        this(as, person, EarthView.DEEP);
    }

    public PersonView(EarthAs as, EarthThing person, EarthView view) {
        super(as, person, view);

        final EarthStore earthStore = use(EarthStore.class);

        firstName = person.getString(EarthField.FIRST_NAME);
        lastName = person.getString(EarthField.LAST_NAME);
        imageUrl = person.getString(EarthField.IMAGE_URL);
        googleUrl = person.getString(EarthField.GOOGLE_URL);

        if (as.hasUser() && person.key().equals(as.getUser().key())) {
            auth = person.getString(EarthField.TOKEN);
        } else {
            auth = null;
        }

        if (as.hasUser() && as.getUser().has(EarthField.GEO) && person.has(EarthField.GEO)) {
            infoDistance = Util.distance(as.getUser().getGeo(EarthField.GEO), person.getGeo(EarthField.GEO));

            boolean isBacking = use(FollowerMine.class).getFollower(person, as.getUser()) != null;

            if (isBacking) {
                EarthGeo latLng = person.getGeo(EarthField.GEO);
                geo = new EarthGeo((float) latLng.getLatitude(), (float) latLng.getLongitude());
            } else {
                geo = null;
            }
        } else {
            infoDistance = null;
            geo = null;
        }

        if (person.has(EarthField.AROUND)) {
            infoUpdated = person.getDate(EarthField.AROUND);
        } else {
            infoUpdated = null;
        }

        switch (view) {
            case DEEP:
                createdOn = person.getDate(EarthField.CREATED_ON);
                socialMode = Util.findHighestSocialMode(use(DeviceMine.class).forUser(person.key().name()));
                infoFollowers = earthStore.count(EarthKind.FOLLOWER_KIND, EarthField.TARGET, person.key());
                infoFollowing = earthStore.count(EarthKind.FOLLOWER_KIND, EarthField.SOURCE, person.key());
                infoOffers = null;

                break;
            default:
                socialMode = null;
                infoFollowers = null;
                infoFollowing = null;
                createdOn = null;
                infoOffers = earthStore.count(EarthKind.OFFER_KIND, EarthField.SOURCE, person.key());

        }
    }
}
