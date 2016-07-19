package com.queatz.snappy.logic.editors;

import com.google.cloud.datastore.DateTime;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.LatLng;
import com.google.gson.JsonObject;
import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthControl;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthJson;
import com.queatz.snappy.logic.EarthKind;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.snappy.shared.Config;

import java.util.Date;

/**
 * Created by jacob on 5/14/16.
 */
public class PartyEditor extends EarthControl {
    private final EarthStore earthStore;
    private final EarthJson earthJson;
    private final LocationEditor locationEditor;
    private final UpdateEditor updateEditor;

    public PartyEditor(final EarthAs as) {
        super(as);

        earthStore = use(EarthStore.class);
        earthJson = use(EarthJson.class);
        locationEditor = use(LocationEditor.class);
        updateEditor = use(UpdateEditor.class);
    }

    public Entity newParty(String original,
                           Entity person,
                           String name,
                           Date date,
                           String locationParam,
                           String details) {

        Entity location = null;

        if(locationParam.startsWith("{")) {
            JsonObject jsonObject = earthJson.fromJson(locationParam, JsonObject.class);

            location = locationEditor.newLocation(
                    jsonObject.get(Config.PARAM_NAME).getAsString(),
                    jsonObject.get(Config.PARAM_ADDRESS).getAsString(),
                    LatLng.of(jsonObject.get(Config.PARAM_LATITUDE).getAsDouble(), jsonObject.get(Config.PARAM_LONGITUDE).getAsDouble()));
        }

        if(location == null) {
            location = earthStore.get(locationParam);
        }

        Entity party = earthStore.create(EarthKind.PARTY_KIND);

        Entity.Builder partyEdit = earthStore.edit(party)
                .set(EarthField.NAME, name)
                .set(EarthField.DATE, DateTime.copyFrom(date))
                .set(EarthField.ABOUT, details)
                .set(EarthField.FULL, false)
                .set(EarthField.HOST, person)
                .set(EarthField.GEO, location.key());


        if (original != null) {
            partyEdit.set(EarthField.ORIGINAL, earthStore.key(original));
        }

        party = earthStore.save(partyEdit);

        updateEditor.newUpdate(person, Config.UPDATE_ACTION_HOST_PARTY, party);

        return party;
    }

    public Entity setFull(Entity party) {
        return earthStore.save(earthStore.edit(party).set(EarthField.FULL, true));
    }
}
