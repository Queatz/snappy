package com.queatz.snappy.logic.interfaces;

import com.queatz.snappy.backend.ApiUtil;
import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthStore;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.logic.concepts.Interfaceable;
import com.queatz.snappy.logic.exceptions.LogicException;
import com.queatz.snappy.logic.exceptions.NothingLogicResponse;
import com.queatz.snappy.logic.views.SuccessView;
import com.queatz.snappy.shared.Config;

import java.io.IOException;

/**
 * Created by jacob on 5/14/16.
 */
public class LocationInterface implements Interfaceable {

    @Override
    public String get(EarthAs as) {
        switch (as.getRoute().size()) {
            case 2:
                switch (as.getRoute().get(1)) {
                    case Config.PATH_PHOTO:
                        getPhoto(as, as.getRoute().get(0));
                        return null;
                }
        }

        throw new NothingLogicResponse("location - bad path");
    }

    @Override
    public String post(EarthAs as) {
        switch (as.getRoute().size()) {
            case 2:
                switch (as.getRoute().get(1)) {
                    case Config.PATH_PHOTO:
                        return putPhoto(as, as.getRoute().get(0));
                }
        }

        throw new NothingLogicResponse("location - bad path");
    }


    private void getPhoto(EarthAs as, String locationId) {
        try {
            if (!ApiUtil.getPhoto(locationId, as.getApi(), as.getRequest(), as.getResponse())) {
                throw new NothingLogicResponse("location photo - not found");
            }
        } catch (IOException e) {
            throw new LogicException("location - io error");
        }
    }

    private String putPhoto(EarthAs as, String locationId) {
        EarthThing location = new EarthStore(as).get(locationId);

        try {
            if (!ApiUtil.putPhoto(location.key().name(), as.getApi(), as.getRequest())) {
                throw new LogicException("location photo - not all good");
            }
        } catch (IOException e) {
            throw new LogicException("location photo - not all good");
        }

        return new SuccessView(true).toJson();
    }
}
