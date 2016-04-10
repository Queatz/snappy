package com.queatz.snappy.logic.things;

import com.google.gcloud.datastore.Entity;
import com.queatz.snappy.backend.ApiUtil;
import com.queatz.snappy.backend.PrintingError;
import com.queatz.snappy.logic.Earth;
import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthField;
import com.queatz.snappy.logic.EarthSingleton;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.snappy.logic.concepts.Interfaceable;
import com.queatz.snappy.logic.editors.HubEditor;
import com.queatz.snappy.logic.exceptions.NothingLogicResponse;
import com.queatz.snappy.logic.view.HubView;
import com.queatz.snappy.service.Api;
import com.queatz.snappy.shared.Config;

import java.io.IOException;
import java.util.Date;

/**
 * Created by jacob on 4/1/16.
 */

public class HubInterface implements Interfaceable {

    private final EarthStore earthStore = EarthSingleton.of(EarthStore.class);
    private final HubEditor hubEditor = EarthSingleton.of(HubEditor.class);

    @Override
    public String get(EarthAs as) {
        switch (as.getRoute().size()) {
            case 0:
                throw new NothingLogicResponse("hub - empty route");
            case 1:
                Entity hub = earthStore.get(as.getRoute().get(0));

                return new HubView(hub).toJson();
            case 2:
                switch (as.getRoute().get(1)) {
                    case Config.PATH_PHOTO:
                        getPhoto(as);
                        return null;
                    default:
                        throw new NothingLogicResponse("hub - bad path");
                }
            default:
                throw new NothingLogicResponse("hub - bad path");
        }
    }

    @Override
    public String post(EarthAs as) {
        if (as.getRoute().isEmpty()) {
            String[] name = as.getParameters().get(EarthField.NAME);
            String[] address = as.getParameters().get(EarthField.ADDRESS);

            if (name == null
                    || address == null
                    || name.length != 1
                    || address.length != 1) {
                throw new NothingLogicResponse("hub - name, address, and about parameters are expected");
            }

            Entity hub = hubEditor.newHub(name[0], address[0]);

            return new HubView(hub).toJson();
        }

        else if(as.getRoute().size() == 1) {
            Entity hub = earthStore.get(as.getRoute().get(0));

            String[] name = as.getParameters().get(EarthField.NAME);
            String[] address = as.getParameters().get(EarthField.ADDRESS);
            String[] about = as.getParameters().get(EarthField.ABOUT);

            hubEditor.edit(hub, extract(name), extract(address), extract(about));
        }

        else if (as.getRoute().size() == 2 && Config.PATH_PHOTO.equals(as.getRoute().get(1))) {
            postPhoto(as);
            return null;
        }

        return null;
    }

    private String extract(String[] param) {
        return param == null || param.length != 1 ? null : param[0];
    }

    private void getPhoto(EarthAs as) {
        Entity thing = earthStore.get(as.getRoute().get(0));

        if (!thing.getBoolean(EarthField.PHOTO)) {
            throw new PrintingError(Api.Error.NOT_FOUND, "hub - photo not set");
        }

        try {
            if(!ApiUtil.getPhoto("earth/thing/photo/" + thing.key().name() + "/", as.getApi(), as.getRequest(), as.getResponse())) {
                throw new PrintingError(Api.Error.NOT_FOUND, "hub - no photo");
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new PrintingError(Api.Error.NOT_FOUND, "hub - photo io error");
        }
    }

    private void postPhoto(EarthAs as) {
        Entity thing = earthStore.get(as.getRoute().get(0));

        try {
            boolean photo = ApiUtil.putPhoto("earth/thing/photo/" + thing.key().name() + "/" + new Date().getTime(), as.getApi(), as.getRequest());

            earthStore.save(earthStore.edit(thing).set(EarthField.PHOTO, photo));
        } catch (IOException e) {
            e.printStackTrace();
            throw new PrintingError(Api.Error.NOT_FOUND, "hub - photo io error");
        }
    }
}
