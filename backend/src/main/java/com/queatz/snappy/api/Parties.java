package com.queatz.snappy.api;

import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Results;
import com.google.appengine.api.search.ScoredDocument;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.queatz.snappy.service.Api;
import com.queatz.snappy.service.Config;
import com.queatz.snappy.service.PrintingError;
import com.queatz.snappy.service.Search;
import com.queatz.snappy.service.Util;

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

                api.snappy.things.person.updateLocation(user, latitude, longitude);

                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

                Results<ScoredDocument> results = api.snappy.search.index.get(Search.Type.PARTY).search("(host = \"" + user + "\" OR distance(loc_cache, geopoint(" + latitude + ", " + longitude + ")) < " + Config.SEARCH_DISTANCE + ") AND date >= \"" + format.format(new Date(new Date().getTime() - 1000 * 60 * 60)) + "\"");
                Iterator<ScoredDocument> iterator = results.iterator();

                while (iterator.hasNext()) {
                    r.put(api.snappy.things.party.toJson(iterator.next(), user, false));
                }

                resp.getWriter().write(r.toString());

                break;

            case POST:
                String localId = req.getParameter(Config.PARAM_LOCAL_ID);

                Document document = api.snappy.things.party.createFromRequest(req, user);

                if(document != null) {
                    JSONObject response = api.snappy.things.party.toJson(document, user, false);
                    Util.localId(response, localId);

                    resp.getWriter().write(response.toString());
                }

                break;
            default:
                throw new PrintingError(Api.Error.NOT_AUTHENTICATED, "parties - bad method");
        }
    }
}

