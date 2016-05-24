package com.queatz.snappy.logic.interfaces;

import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.LatLng;
import com.queatz.snappy.backend.ApiUtil;
import com.queatz.snappy.backend.PrintingError;
import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthSingleton;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.snappy.logic.concepts.Interfaceable;
import com.queatz.snappy.logic.editors.ContactEditor;
import com.queatz.snappy.logic.editors.HubEditor;
import com.queatz.snappy.logic.exceptions.NothingLogicResponse;
import com.queatz.snappy.logic.views.HubView;
import com.queatz.snappy.service.Api;
import com.queatz.snappy.shared.Config;

import java.io.IOException;
import java.util.Date;

/**
 * Created by jacob on 4/1/16.
 */

public class HubInterface extends CommonThingInterface {

    private final HubEditor hubEditor = EarthSingleton.of(HubEditor.class);

    @Override
    public Entity createThing(EarthAs as) {
        String[] name = as.getParameters().get(EarthField.NAME);
        String[] address = as.getParameters().get(EarthField.ADDRESS);
        String[] latitude = as.getParameters().get(EarthField.LATITUDE);
        String[] longitude = as.getParameters().get(EarthField.LONGITUDE);

        if (name == null
                || address == null
                || latitude == null
                || longitude == null
                || name.length != 1
                || address.length != 1
                || latitude.length != 1
                || longitude.length != 1) {
            throw new NothingLogicResponse("hub - name, address, geo, and about parameters are expected");
        }

        return hubEditor.newHub(name[0], address[0],
                LatLng.of(Double.valueOf(latitude[0]), Double.valueOf(longitude[0])));
    }

    @Override
    public Entity editThing(EarthAs as, Entity hub) {
        String[] name = as.getParameters().get(EarthField.NAME);
        String[] address = as.getParameters().get(EarthField.ADDRESS);
        String[] about = as.getParameters().get(EarthField.ABOUT);
        String latitude = extract(as.getParameters().get(EarthField.LATITUDE));
        String longitude = extract(as.getParameters().get(EarthField.LONGITUDE));

        LatLng latLng = null;
        if (latitude != null && longitude != null) {
            latLng = LatLng.of(Double.valueOf(latitude), Double.valueOf(longitude));
        }

        return hubEditor.edit(hub, extract(name), extract(address), latLng, extract(about));
    }

    private String extract(String[] param) {
        return param == null || param.length != 1 ? null : param[0];
    }
}
