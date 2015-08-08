package com.queatz.snappy.api;

import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.GeoPoint;
import com.google.appengine.api.search.Results;
import com.google.appengine.api.search.ScoredDocument;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.queatz.snappy.service.Api;
import com.queatz.snappy.service.Buy;
import com.queatz.snappy.backend.Config;
import com.queatz.snappy.backend.PrintingError;
import com.queatz.snappy.service.Push;
import com.queatz.snappy.service.Search;
import com.queatz.snappy.service.Things;
import com.queatz.snappy.backend.Util;
import com.queatz.snappy.thing.Location;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by jacob on 2/8/15.
 */

public class Parties implements Api.Path {
    Api api;

    public Parties(Api a) {
        api = a;
    }

    @Override
    public void call(ArrayList<String> path, String user, HTTPMethod method, HttpServletRequest req, HttpServletResponse resp) throws IOException, PrintingError {
        switch (method) {
            case GET:
                String latitudeParameter = req.getParameter(Config.PARAM_LATITUDE);
                String longitudeParameter = req.getParameter(Config.PARAM_LONGITUDE);

                JSONArray r = new JSONArray();

                if(longitudeParameter == null || latitudeParameter == null) {
                    throw new PrintingError(Api.Error.NOT_IMPLEMENTED, "missing location");
                }

                double latitude = Double.parseDouble(latitudeParameter);
                double longitude = Double.parseDouble(longitudeParameter);

                Things.getService().person.updateLocation(user, latitude, longitude);

                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

                Results<ScoredDocument> results = Search.getService().index.get(Search.Type.PARTY).search("(host = \"" + user + "\" OR full=\"" + Boolean.toString(false) + "\") AND date >= \"" + format.format(new Date(new Date().getTime() - 1000 * 60 * 60)) + "\"");

                for (ScoredDocument result : results) {
                    r.put(Things.getService().party.toJson(result, user, false));

                    if (r.length() >= Config.SEARCH_MINIMUM) {
                        GeoPoint point = Things.getService().location.getGeoPoint(result);

                        if (Util.distance(latitude, longitude, point.getLatitude(), point.getLongitude()) > Config.SEARCH_DISTANCE)
                            break;
                    }
                }

                resp.getWriter().write(r.toString());

                break;

            case POST:
                if(!Buy.getService().valid(user))
                    throw new PrintingError(Api.Error.NOT_FOUND, "parties - not bought");

                String localId = req.getParameter(Config.PARAM_LOCAL_ID);

                Document document = Things.getService().party.createFromRequest(req, user);

                if(document != null) {
                    JSONObject response = Things.getService().party.toJson(document, user, false);
                    Util.localId(response, localId);

                    Push.getService().sendToFollowers(user, Things.getService().party.makePush(document));
                    resp.getWriter().write(response.toString());
                }

                break;
            default:
                throw new PrintingError(Api.Error.NOT_AUTHENTICATED, "parties - bad method");
        }
    }
}

