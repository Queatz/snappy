package com.queatz.snappy.api;

import com.google.appengine.api.datastore.GeoPt;
import com.queatz.snappy.backend.Datastore;
import com.queatz.snappy.service.Api;
import com.queatz.snappy.service.Search;
import com.queatz.snappy.service.Thing;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.shared.HereResponseSpec;
import com.queatz.snappy.shared.things.LocationSpec;
import com.queatz.snappy.shared.things.PartySpec;
import com.queatz.snappy.shared.things.PersonSpec;
import com.queatz.snappy.shared.things.QuestSpec;

import java.util.Date;
import java.util.List;

/**
 * Created by jacob on 8/21/15.
 */
public class Here extends Api.Path {
    public Here(Api api) {
        super(api);
    }

    @Override
    public void call() {
        switch (method) {
            case GET:
                get(request.getParameter(Config.PARAM_LATITUDE), request.getParameter(Config.PARAM_LONGITUDE));

                break;
            default:
                die("here - bad method");
        }
    }

    private void get(String latitudeParameter, String longitudeParameter) {
        if (longitudeParameter == null || latitudeParameter == null) {
            die("here - missing location parameter(s)");
        }

        float latitude = Float.parseFloat(latitudeParameter);
        float longitude = Float.parseFloat(longitudeParameter);
        GeoPt geo = new GeoPt(latitude, longitude);

        Thing.getService().person.updateLocation(user.id, geo);

        HereResponseSpec response = new HereResponseSpec();

        response.parties = fetchParties(user.id, geo);
        response.people = fetchPeople(geo);
        response.locations = fetchLocations(geo);
        response.quests = fetchQuests(geo);

        ok(response);
    }

    private List<LocationSpec> fetchLocations(GeoPt geo) {
        return Search.getService().getNearby(LocationSpec.class, geo, null, 1);
    }

    private List<PersonSpec> fetchPeople(GeoPt geo) {
        return Search.getService().getNearby(PersonSpec.class, geo, new Date(new Date().getTime() - 1000 * 60 * 60), Config.SEARCH_PEOPLE_MAX_NEAR_HERE);
    }

    private List<PartySpec> fetchParties(String user, GeoPt geo) {
        Date anHourAgo = new Date(new Date().getTime() - 1000 * 60 * 60);

        List<PartySpec> parties = Search.getService().getNearby(PartySpec.class, geo, anHourAgo, Config.SEARCH_MAXIMUM);

        for(PartySpec party : Datastore.ofy().load().type(PartySpec.class).filter("hostId", user).filter("date >=", anHourAgo).list()) {
            if (!parties.contains(party)) {
                parties.add(party);
            }
        }

        return parties;
    }

    private List<QuestSpec> fetchQuests(GeoPt geo) {
        return Search.getService().getNearby(QuestSpec.class, geo, new Date(new Date().getTime() - Config.QUESTS_MAX_AGE), Config.NEARBY_MAX_VISIBILITY);
    }
}

