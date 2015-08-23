package com.queatz.snappy.api;

import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.GeoPoint;
import com.google.appengine.api.search.Query;
import com.google.appengine.api.search.QueryOptions;
import com.google.appengine.api.search.Results;
import com.google.appengine.api.search.ScoredDocument;
import com.google.appengine.api.search.SortExpression;
import com.google.appengine.api.search.SortOptions;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.queatz.snappy.backend.Config;
import com.queatz.snappy.backend.PrintingError;
import com.queatz.snappy.backend.Util;
import com.queatz.snappy.service.Api;
import com.queatz.snappy.service.Buy;
import com.queatz.snappy.service.Push;
import com.queatz.snappy.service.Search;
import com.queatz.snappy.service.Things;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by jacob on 8/21/15.
 */
public class Here implements Api.Path {
    Api api;

    public Here(Api a) {
        api = a;
    }

    private JSONArray fetchLocations(String user, double latitude, double longitude) {
        JSONArray r = new JSONArray();

        String queryString = "distance(location, geopoint(" + latitude + ", " + longitude + ")) < " + Config.SEARCH_MAX_VISIBILITY;

        SortOptions sortOptions = SortOptions.newBuilder().addSortExpression(
                SortExpression.newBuilder().setExpression("distance(location, geopoint(" + latitude + ", " + longitude + "))").setDirection(SortExpression.SortDirection.ASCENDING).build()
        ).build();

        QueryOptions queryOptions = QueryOptions.newBuilder().setSortOptions(sortOptions).setLimit(Config.SEARCH_LOCATIONS_MAX_HERE).build();

        Query query = Query.newBuilder().setOptions(queryOptions).build(queryString);

        Results<ScoredDocument> results = Search.getService().index.get(Search.Type.LOCATION).search(query);

        if(results.getNumberReturned() > 0) {
            r.put(Things.getService().location.toJson(results.iterator().next(), user, false));
        }

        return r;
    }

    private JSONArray fetchPeople(String user, double latitude, double longitude) {
        JSONArray r = new JSONArray();

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

        Date oneHourAgo = new Date(new Date().getTime() - 1000 * 60 * 60);

        String queryString = "distance(latlng, geopoint(" + latitude + ", " + longitude + ")) < " + Config.SEARCH_PEOPLE_MAX_DISTANCE + " AND around >= \"" + format.format(oneHourAgo) + "\"";

        SortOptions sortOptions = SortOptions.newBuilder().addSortExpression(
                SortExpression.newBuilder().setExpression("distance(latlng, geopoint(" + latitude + ", " + longitude + "))").setDirection(SortExpression.SortDirection.ASCENDING).build()
        ).build();

        QueryOptions queryOptions = QueryOptions.newBuilder().setSortOptions(sortOptions).setLimit(Config.SEARCH_PEOPLE_MAX_NEAR_HERE).build();

        Query query = Query.newBuilder().setOptions(queryOptions).build(queryString);

        Results<ScoredDocument> results = Search.getService().index.get(Search.Type.PERSON).search(query);

        for (ScoredDocument result : results) {
            if(user.equals(result.getId()) || result.getOnlyField("around").getDate().before(oneHourAgo))
                continue;

            r.put(Things.getService().person.toJson(result, user, true));
        }

        return r;
    }

    private JSONArray fetchParties(String user, double latitude, double longitude) {
        JSONArray r = new JSONArray();

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

        // TODO: Audit that this is actually sorting and then matching, not matching and then sorting
        String queryString = "(host = \"" + user + "\" OR (distance(loc_cache, geopoint(" + latitude + ", " + longitude + ")) < " + Config.SEARCH_MAX_VISIBILITY + " AND full=\"" + Boolean.toString(false) + "\")) AND date >= \"" + format.format(new Date(new Date().getTime() - 1000 * 60 * 60)) + "\"";

        SortOptions sortOptions = SortOptions.newBuilder().addSortExpression(
                SortExpression.newBuilder().setExpression("distance(loc_cache, geopoint(" + latitude + ", " + longitude + "))").setDirection(SortExpression.SortDirection.ASCENDING).build()
        ).build();

        QueryOptions queryOptions = QueryOptions.newBuilder().setSortOptions(sortOptions).setLimit(Config.SEARCH_MAXIMUM).build();

        Query query = Query.newBuilder().setOptions(queryOptions).build(queryString);

        Results<ScoredDocument> results = Search.getService().index.get(Search.Type.PARTY).search(query);

        for (ScoredDocument result : results) {
            r.put(Things.getService().party.toJson(result, user, false));

            if (r.length() >= Config.SEARCH_MINIMUM) {
                GeoPoint point = Things.getService().location.getGeoPoint(result);

                if (Util.distance(latitude, longitude, point.getLatitude(), point.getLongitude()) > Config.SEARCH_DISTANCE)
                    break;
            }
        }

        return r;
    }

    @Override
    public void call(ArrayList<String> path, String user, HTTPMethod method, HttpServletRequest req, HttpServletResponse resp) throws IOException, PrintingError {
        switch (method) {
            case GET:
                String latitudeParameter = req.getParameter(Config.PARAM_LATITUDE);
                String longitudeParameter = req.getParameter(Config.PARAM_LONGITUDE);

                if(longitudeParameter == null || latitudeParameter == null) {
                    throw new PrintingError(Api.Error.NOT_IMPLEMENTED, "missing location");
                }

                double latitude = Double.parseDouble(latitudeParameter);
                double longitude = Double.parseDouble(longitudeParameter);

                Things.getService().person.updateLocation(user, latitude, longitude);

                JSONObject jsonObject = new JSONObject();

                try {
                    jsonObject.put("parties", fetchParties(user, latitude, longitude));
                    jsonObject.put("people", fetchPeople(user, latitude, longitude));
                    jsonObject.put("locations", fetchLocations(user, latitude, longitude));
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }

                resp.getWriter().write(jsonObject.toString());

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

