package com.queatz.snappy.api;

import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Field;
import com.google.appengine.api.search.GetIndexesRequest;
import com.google.appengine.api.search.GetRequest;
import com.google.appengine.api.search.GetResponse;
import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.PutException;
import com.google.appengine.api.search.Query;
import com.google.appengine.api.search.QueryOptions;
import com.google.appengine.api.search.Results;
import com.google.appengine.api.search.ScoredDocument;
import com.google.appengine.api.search.SearchServiceFactory;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.queatz.snappy.backend.Config;
import com.queatz.snappy.backend.Util;
import com.queatz.snappy.service.Api;
import com.queatz.snappy.backend.PrintingError;
import com.queatz.snappy.service.Search;
import com.queatz.snappy.service.Things;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by jacob on 2/8/15.
 */

public class Pirate implements Api.Path {
    Api api;

    public Pirate(Api a) {
        api = a;
    }

    @Override
    public void call(ArrayList<String> path, String user, HTTPMethod method, HttpServletRequest req, HttpServletResponse resp) throws IOException, PrintingError {
        Query query = Query.newBuilder().setOptions(QueryOptions.newBuilder().setLimit(1000).build()).build("distance(latlng, geopoint(0, 0)) > 0");
        Results<ScoredDocument> results = Search.getService().index.get(Search.Type.BOUNTY).search(query);

        for(ScoredDocument doc : results) {
            Search.getService().index.get(Search.Type.BOUNTY).delete(doc.getId());
        }

        resp.getWriter().write("yarr!");
    }
}