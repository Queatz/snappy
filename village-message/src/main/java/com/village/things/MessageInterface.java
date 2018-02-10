package com.village.things;

import com.image.SnappyImage;
import com.queatz.earth.EarthField;
import com.queatz.earth.EarthQueries;
import com.queatz.earth.EarthStore;
import com.queatz.earth.EarthThing;
import com.queatz.earth.FrozenQuery;
import com.queatz.snappy.api.ApiUtil;
import com.queatz.snappy.as.EarthAs;
import com.queatz.snappy.exceptions.Error;
import com.queatz.snappy.exceptions.NothingLogicResponse;
import com.queatz.snappy.exceptions.PrintingError;
import com.queatz.snappy.router.Interfaceable;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.shared.EarthJson;
import com.vlllage.graph.EarthGraph;

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
                String select = as.getParameters().containsKey(Config.PARAM_SELECT) ? as.getParameters().get(Config.PARAM_SELECT)[0] : null;
                FrozenQuery query = as.s(EarthQueries.class).byId(as.getRoute().get(0));

                return as.s(EarthJson.class).toJson(
                        as.s(EarthGraph.class).queryOne(query.getEarthQuery(), select, query.getVars())
                );
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
        EarthThing thing = as.s(EarthStore.class).get(as.getRoute().get(0));

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
