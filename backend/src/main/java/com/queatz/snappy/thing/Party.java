package com.queatz.snappy.thing;

import com.google.appengine.api.datastore.GeoPt;
import com.queatz.snappy.backend.Datastore;
import com.queatz.snappy.backend.Json;
import com.queatz.snappy.backend.Util;
import com.queatz.snappy.service.Thing;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.shared.things.LocationSpec;
import com.queatz.snappy.shared.things.PartySpec;
import com.queatz.snappy.shared.things.PersonSpec;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by jacob on 2/15/15.
 */
public class Party {
    public PartySpec createFromRequest(HttpServletRequest req, PersonSpec user) {
        String original = req.getParameter(Config.PARAM_ID);
        String name = req.getParameter(Config.PARAM_NAME);
        Date date = Util.stringToDate(req.getParameter(Config.PARAM_DATE));
        String locationParam = req.getParameter(Config.PARAM_LOCATION);
        String details = req.getParameter(Config.PARAM_DETAILS);

        LocationSpec location = null;

        if(locationParam.startsWith("{")) {
            location = Json.from(locationParam, LocationSpec.class);
            location.latlng = new GeoPt(location.latitude, location.longitude);
            Datastore.save(location);
        }

        if(location == null) {
            location = Datastore.get(LocationSpec.class, locationParam);
        }

        PartySpec party = Datastore.create(PartySpec.class);

        party.locationId = Datastore.key(location);

        if (original != null) {
            party.originalId = Datastore.key(PartySpec.class, original);
        }

        party.name = name;
        party.date = date;
        party.latlng = location.latlng;
        party.details = details;
        party.hostId = Datastore.key(user);
        party.full = false;

        Datastore.save(party);
        Thing.getService().update.create(Config.UPDATE_ACTION_HOST_PARTY, user, party);

        return party;
    }

    public void setFull(PartySpec party) {
        party.full = true;
        Datastore.save(party);
    }
}
