package com.queatz.snappy.logic.views;

import com.google.cloud.datastore.Entity;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthKind;
import com.queatz.snappy.logic.EarthSingleton;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.snappy.logic.EarthView;
import com.queatz.snappy.logic.concepts.Viewable;

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
    final boolean infoAvailability;
    final double infoDistance;
    final List<Viewable> updates;
    final List<Viewable> offers;
    final List<Viewable> resources;
    final List<Viewable> projects;
    final List<Viewable> hubs;
    final List<Viewable> clubs;

    public PersonView(Entity person) {
        this(person, EarthView.DEEP);
    }

    public PersonView(Entity person, EarthView view) {
        super(person, view);

        final EarthStore earthStore = EarthSingleton.of(EarthStore.class);

        firstName = person.getString(EarthField.FIRST_NAME);
        lastName = person.getString(EarthField.LAST_NAME);
        about = person.getString(EarthField.ABOUT);
        imageUrl = person.getString(EarthField.IMAGE_URL);
        googleUrl = person.getString(EarthField.GOOGLE_URL);

        // XXX TODO only if it's us
        auth = person.getString(EarthField.TOKEN);

        infoAvailability = true;
        infoDistance = 0;

        switch (view) {
            case DEEP:
                List<Entity> updatesList = earthStore.find(EarthKind.UPDATE_KIND, EarthField.TARGET, person.key());
                List<Entity> offersList = earthStore.find(EarthKind.OFFER_KIND, EarthField.SOURCE, person.key());
                List<Entity> resourcesList = earthStore.find(EarthKind.RESOURCE_KIND, EarthField.SOURCE, person.key());
                List<Entity> projectsList = earthStore.find(EarthKind.PROJECT_KIND, EarthField.SOURCE, person.key());
                List<Entity> hubsList = earthStore.find(EarthKind.HUB_KIND, EarthField.SOURCE, person.key());
                List<Entity> clubsList = earthStore.find(EarthKind.CLUB_KIND, EarthField.SOURCE, person.key());

                updates = new EntityListView(updatesList, EarthView.SHALLOW).asList();
                offers = new EntityListView(offersList, EarthView.SHALLOW).asList();
                resources = new EntityListView(resourcesList, EarthView.SHALLOW).asList();
                projects = new EntityListView(projectsList, EarthView.SHALLOW).asList();
                hubs = new EntityListView(hubsList, EarthView.SHALLOW).asList();
                clubs = new EntityListView(clubsList, EarthView.SHALLOW).asList();

                infoFollowers = earthStore.count(EarthKind.FOLLOWER_KIND, EarthField.TARGET, person.key());
                infoFollowing = earthStore.count(EarthKind.FOLLOWER_KIND, EarthField.SOURCE, person.key());
                infoOffers = null;

                break;
            default:
                updates = null;
                offers = null;
                resources = null;
                projects = null;
                hubs = null;
                clubs = null;
                infoFollowers = null;
                infoFollowing = null;
                infoOffers = earthStore.count(EarthKind.OFFER_KIND, EarthField.SOURCE, person.key());

        }
    }
}
