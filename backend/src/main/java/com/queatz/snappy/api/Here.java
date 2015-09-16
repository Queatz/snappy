package com.queatz.snappy.api;

import com.google.appengine.api.search.GeoPoint;
import com.google.appengine.api.search.Query;
import com.google.appengine.api.search.QueryOptions;
import com.google.appengine.api.search.Results;
import com.google.appengine.api.search.ScoredDocument;
import com.google.appengine.api.search.SortExpression;
import com.google.appengine.api.search.SortOptions;
import com.queatz.snappy.backend.Config;
import com.queatz.snappy.backend.PrintingError;
import com.queatz.snappy.backend.Util;
import com.queatz.snappy.service.Api;
import com.queatz.snappy.service.Search;
import com.queatz.snappy.service.Things;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by jacob on 8/21/15.
 */
public class Here extends Api.Path {
    public Here(Api api) {
        super(api);
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

        if (results.getNumberReturned() > 0) {
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
            if (user.equals(result.getId()) || result.getOnlyField("around").getDate().before(oneHourAgo))
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

    private JSONArray fetchBounties(String user, double latitude, double longitude) {
        JSONArray r = new JSONArray();

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

        String queryString = "distance(latlng, geopoint(" + latitude + ", " + longitude + ")) < " + Config.BOUNTIES_MAX_VISIBILITY + " AND posted >= \"" + format.format(new Date(new Date().getTime() - Config.BOUNTIES_MAX_AGE)) + "\"";

        QueryOptions queryOptions = QueryOptions.newBuilder().setLimit(Config.BOUNTIES_MAXIMUM).build();
        Query query = Query.newBuilder().setOptions(queryOptions).build(queryString);
        Results<ScoredDocument> results = Search.getService().index.get(Search.Type.BOUNTY).search(query);

        for (ScoredDocument result : results) {
            r.put(Things.getService().bounty.toJson(result, user, false));
        }

        return r;
    }

    @Override
    public void call() throws IOException, PrintingError {
        switch (method) {
            case GET:
                get(request.getParameter(Config.PARAM_LATITUDE), request.getParameter(Config.PARAM_LONGITUDE));

                break;
            default:
                die("here - bad method");
        }
    }

    private void get(String latitudeParameter, String longitudeParameter) throws IOException, PrintingError {
        if (longitudeParameter == null || latitudeParameter == null) {
            die("here - missing location parameter(s)");
        }

        double latitude = Double.parseDouble(latitudeParameter);
        double longitude = Double.parseDouble(longitudeParameter);

        Things.getService().person.updateLocation(user, latitude, longitude);

        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("parties", fetchParties(user, latitude, longitude));
            jsonObject.put("people", fetchPeople(user, latitude, longitude));
            jsonObject.put("locations", fetchLocations(user, latitude, longitude));
            jsonObject.put("bounties", fetchBounties(user, latitude, longitude));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        response.getWriter().write(jsonObject.toString());
    }
}

