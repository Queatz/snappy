package com.queatz.snappy.api;

import com.google.appengine.api.datastore.GeoPt;
import com.queatz.snappy.backend.Json;
import com.queatz.snappy.service.Api;
import com.queatz.snappy.service.Search;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.shared.things.LocationSpec;

import java.util.List;

/**
 * Created by jacob on 8/11/15.
 */
public class Locations extends Api.Path {
    public Locations(Api api) {
        super(api);
    }

    @Override
    public void call() {
        switch (method) {
            case GET:
                if (path.size() > 0) {
                    die("locations - bad path");
                }

                String paramLatitude = request.getParameter(Config.PARAM_LATITUDE);
                String paramLongitude = request.getParameter(Config.PARAM_LONGITUDE);
                String name = request.getParameter(Config.PARAM_NAME);

                get(paramLatitude, paramLongitude, name);

                break;
            default:
                die("locations - bad method");
        }
    }

    private void get(String paramLatitude, String paramLongitude, String name) {
        if (paramLatitude == null || paramLongitude == null || name == null) {
            die("locations - bad parameters");
        }

        float latitude = Float.parseFloat(paramLatitude);
        float longitude = Float.parseFloat(paramLongitude);
        List<LocationSpec> locations = Search.getService().getNearby(LocationSpec.class, new GeoPt(latitude, longitude), null, Config.SUGGESTION_LIMIT);

        ok(locations, Json.Compression.SHALLOW);
    }
}
