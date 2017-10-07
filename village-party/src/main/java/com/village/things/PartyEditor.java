package com.village.things;

import com.google.gson.JsonObject;
import com.queatz.earth.EarthField;
import com.queatz.earth.EarthKind;
import com.queatz.earth.EarthStore;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.as.EarthAs;
import com.queatz.snappy.as.EarthControl;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.shared.EarthJson;
import com.queatz.snappy.shared.earth.EarthGeo;
import com.queatz.snappy.shared.earth.EarthRef;

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
        earthJson = new EarthJson();
        locationEditor = use(LocationEditor.class);
        updateEditor = use(UpdateEditor.class);
    }

    public EarthThing newParty(String original,
                               EarthThing person,
                               String name,
                               Date date,
                               String locationParam,
                               String details) {

        EarthThing location = null;

        if(locationParam.startsWith("{")) {
            JsonObject jsonObject = earthJson.fromJson(locationParam, JsonObject.class);

            location = locationEditor.newLocation(
                    jsonObject.get(Config.PARAM_NAME).getAsString(),
                    jsonObject.has(Config.PARAM_ADDRESS) ? jsonObject.get(Config.PARAM_ADDRESS).getAsString() : null,
                    EarthGeo.of(jsonObject.get(Config.PARAM_LATITUDE).getAsDouble(), jsonObject.get(Config.PARAM_LONGITUDE).getAsDouble()));
        }

        if(location == null) {
            location = earthStore.get(locationParam);
        }

        EarthThing party = earthStore.create(EarthKind.PARTY_KIND);

        EarthThing.Builder partyEdit = earthStore.edit(party)
                .set(EarthField.NAME, name)
                .set(EarthField.DATE, date)
                .set(EarthField.ABOUT, details)
                .set(EarthField.FULL, false)
                .set(EarthField.PHOTO, false)
                .set(EarthField.HOST, person.key())
                .set(EarthField.TARGET, location.key()) // Also setting this so that it shows up in location searches
                .set(EarthField.LOCATION, location.key());


        if (original != null) {
            partyEdit.set(EarthField.ORIGINAL, EarthRef.of(original));
        }

        party = earthStore.save(partyEdit);

        updateEditor.newUpdate(person, Config.UPDATE_ACTION_HOST_PARTY, party);

        return party;
    }

    public EarthThing setFull(EarthThing party) {
        return earthStore.save(earthStore.edit(party).set(EarthField.FULL, true));
    }
}
