package com.queatz.snappy.logic.interfaces;

import com.image.SnappyImage;
import com.queatz.snappy.api.ApiUtil;
import com.queatz.snappy.exceptions.PrintingError;
import com.queatz.snappy.as.EarthAs;
import com.queatz.earth.EarthField;
import com.queatz.earth.EarthStore;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.logic.EarthViewer;
import com.queatz.snappy.api.Interfaceable;
import com.queatz.snappy.exceptions.NothingLogicResponse;
import com.queatz.snappy.exceptions.Error;
import com.queatz.snappy.shared.Config;

import java.io.IOException;

/**
 * Created by jacob on 5/9/16.
 */
public class MessageInterface implements Interfaceable {
    @Override
    public String get(EarthAs as) {
        switch (as.getRoute().size()) {
            case 0:
                throw new NothingLogicResponse("message - empty route");
            case 1:
                EarthThing thing = new EarthStore(as).get(as.getRoute().get(0));

                return new EarthViewer(as).getViewForEntityOrThrow(thing).toJson();
            case 2:
                switch (as.getRoute().get(1)) {
                    case Config.PATH_PHOTO:
                        getPhoto(as);
                        return null;
                }
        }

        throw new NothingLogicResponse("message - bad path");
    }

    @Override
    public String post(EarthAs as) {
        return null;
    }

    private void getPhoto(EarthAs as) {
        EarthThing thing = new EarthStore(as).get(as.getRoute().get(0));

        if (!thing.getBoolean(EarthField.PHOTO)) {
            throw new PrintingError(Error.NOT_FOUND, "thing - photo not set");
        }

        try {
            if(!ApiUtil.getPhoto(thing.key().name(), as.s(SnappyImage.class), as.getRequest(), as.getResponse())) {
                throw new PrintingError(Error.NOT_FOUND, "thing - no photo");
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new PrintingError(Error.NOT_FOUND, "thing - photo io error");
        }
    }
}
