package com.queatz.snappy.logic.views;

import com.queatz.snappy.backend.Util;
import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthGeo;
import com.queatz.snappy.logic.EarthKind;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.snappy.logic.EarthThing;
import com.queatz.snappy.logic.EarthView;
import com.queatz.snappy.logic.concepts.Viewable;
import com.queatz.snappy.logic.mines.DeviceMine;
import com.queatz.snappy.logic.mines.FollowerMine;
import com.queatz.snappy.shared.Config;

import java.util.Date;
import java.util.List;

/**
 * Created by jacob on 5/8/16.
 */
public class PersonView extends ExistenceView {

    final String firstName;
    final String lastName;
    final String about;
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
    final List<Viewable> updates;
    final List<Viewable> offers;
    final List<Viewable> resources;
    final List<Viewable> projects;
    final List<Viewable> hubs;
    final List<Viewable> clubs;
    final EarthGeo geo;

    public PersonView(EarthAs as, EarthThing person) {
        this(as, person, EarthView.DEEP);
    }

    public PersonView(EarthAs as, EarthThing person, EarthView view) {
        super(as, person, view);

        final EarthStore earthStore = use(EarthStore.class);

        firstName = person.getString(EarthField.FIRST_NAME);
        lastName = person.getString(EarthField.LAST_NAME);
        about = person.getString(EarthField.ABOUT);
        imageUrl = person.getString(EarthField.IMAGE_URL);
        googleUrl = person.getString(EarthField.GOOGLE_URL);

        if (person.key().equals(as.getUser().key())) {
            auth = person.getString(EarthField.TOKEN);
        } else {
            auth = null;
        }

        if (as.getUser().has(EarthField.GEO) && person.has(EarthField.GEO)) {
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

                List<EarthThing> updatesList = earthStore.find(EarthKind.UPDATE_KIND, EarthField.TARGET, person.key(), Config.SEARCH_MAXIMUM);
                List<EarthThing> offersList = earthStore.find(EarthKind.OFFER_KIND, EarthField.SOURCE, person.key());
                List<EarthThing> resourcesList = earthStore.find(EarthKind.RESOURCE_KIND, EarthField.SOURCE, person.key());
                List<EarthThing> projectsList = earthStore.find(EarthKind.PROJECT_KIND, EarthField.SOURCE, person.key());
                List<EarthThing> hubsList = earthStore.find(EarthKind.HUB_KIND, EarthField.SOURCE, person.key());
                List<EarthThing> clubsList = earthStore.find(EarthKind.CLUB_KIND, EarthField.SOURCE, person.key());

                updates = new EntityListView(as, updatesList, EarthView.DEEP).asList();
                offers = new EntityListView(as, offersList, EarthView.SHALLOW).asList();
                resources = new EntityListView(as, resourcesList, EarthView.SHALLOW).asList();
                projects = new EntityListView(as, projectsList, EarthView.SHALLOW).asList();
                hubs = new EntityListView(as, hubsList, EarthView.SHALLOW).asList();
                clubs = new EntityListView(as, clubsList, EarthView.SHALLOW).asList();

                infoFollowers = earthStore.count(EarthKind.FOLLOWER_KIND, EarthField.TARGET, person.key());
                infoFollowing = earthStore.count(EarthKind.FOLLOWER_KIND, EarthField.SOURCE, person.key());
                infoOffers = null;

                break;
            default:
                socialMode = null;
                updates = null;
                offers = null;
                resources = null;
                projects = null;
                hubs = null;
                clubs = null;
                infoFollowers = null;
                infoFollowing = null;
                createdOn = null;
                infoOffers = earthStore.count(EarthKind.OFFER_KIND, EarthField.SOURCE, person.key());

        }
    }
}
