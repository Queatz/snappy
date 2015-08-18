package com.queatz.snappy.api;

import com.google.appengine.api.search.Query;
import com.google.appengine.api.search.QueryOptions;
import com.google.appengine.api.search.Results;
import com.google.appengine.api.search.ScoredDocument;
import com.google.appengine.api.search.SortExpression;
import com.google.appengine.api.search.SortOptions;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.queatz.snappy.backend.Config;
import com.queatz.snappy.backend.PrintingError;
import com.queatz.snappy.service.Api;
import com.queatz.snappy.service.Search;
import com.queatz.snappy.service.Things;

import org.json.JSONArray;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by jacob on 8/11/15.
 */
public class Locations implements Api.Path {
    Api api;

    public Locations(Api a) {
        api = a;
    }

    @Override
    public void call(ArrayList<String> path, String user, HTTPMethod method, HttpServletRequest req, HttpServletResponse resp) throws IOException, PrintingError {
        switch (method) {
            case GET:
                if(path.size() > 0) {
                    throw new PrintingError(Api.Error.NOT_AUTHENTICATED, "locations - bad path");
                }

                String paramLatitude = req.getParameter(Config.PARAM_LATITUDE);
                String paramLongitude = req.getParameter(Config.PARAM_LONGITUDE);
                String name = req.getParameter(Config.PARAM_NAME);

                if(paramLatitude == null || paramLongitude == null || name == null) {
                    throw new PrintingError(Api.Error.NOT_AUTHENTICATED, "locations = bad parameters");
                }

                double latitude = Double.parseDouble(paramLatitude);
                double longitude = Double.parseDouble(paramLongitude);

                SortOptions sortOptions = SortOptions.newBuilder().addSortExpression(
                        SortExpression.newBuilder().setExpression("distance(location, geopoint(" + latitude + ", " + longitude + "))").setDirection(SortExpression.SortDirection.ASCENDING).build()
                ).build();
                QueryOptions queryOptions = QueryOptions.newBuilder().setSortOptions(sortOptions).setLimit(Config.SUGGESTION_LIMIT).build();
                Query query = Query.newBuilder().setOptions(queryOptions).build("name = ~\"" + name + "\" AND distance(location, geopoint(" + latitude + ", " + longitude + ")) < " + Config.SUGGESTION_MAX_DISTANCE);

                Results<ScoredDocument> results = Search.getService().index.get(Search.Type.LOCATION).search(query);

                JSONArray r = new JSONArray();

                for (ScoredDocument result : results) {
                    r.put(Things.getService().location.toJson(result, user, true));
                }

                resp.getWriter().write(r.toString());

                break;
            default:
                throw new PrintingError(Api.Error.NOT_AUTHENTICATED, "locations - bad method");
        }
    }
}
