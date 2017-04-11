package com.queatz.snappy.logic.interfaces;

import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthGeo;
import com.queatz.snappy.logic.EarthThing;
import com.queatz.snappy.logic.editors.HubEditor;
import com.queatz.snappy.logic.exceptions.NothingLogicResponse;

/**
 * Created by jacob on 4/1/16.
 */

public class HubInterface extends CommonThingInterface {

    @Override
    public EarthThing createThing(EarthAs as) {
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

        return new HubEditor(as).newHub(
                name[0],
                address[0],
                EarthGeo.of(Double.valueOf(latitude[0]), Double.valueOf(longitude[0])),
                as.getUser());
    }

    @Override
    public EarthThing editThing(EarthAs as, EarthThing hub) {
        String[] name = as.getParameters().get(EarthField.NAME);
        String[] address = as.getParameters().get(EarthField.ADDRESS);
        String[] about = as.getParameters().get(EarthField.ABOUT);
        String latitude = extract(as.getParameters().get(EarthField.LATITUDE));
        String longitude = extract(as.getParameters().get(EarthField.LONGITUDE));

        EarthGeo latLng = null;
        if (latitude != null && longitude != null) {
            latLng = EarthGeo.of(Double.valueOf(latitude), Double.valueOf(longitude));
        }

        return new HubEditor(as).edit(hub, extract(name), extract(address), latLng, extract(about));
    }

    private String extract(String[] param) {
        return param == null || param.length != 1 ? null : param[0];
    }
}
