package com.queatz.snappy.api;

import com.google.appengine.api.search.Query;
import com.google.appengine.api.search.QueryOptions;
import com.google.appengine.api.search.Results;
import com.google.appengine.api.search.ScoredDocument;
import com.queatz.snappy.backend.PrintingError;
import com.queatz.snappy.service.Api;
import com.queatz.snappy.service.Search;

import java.io.IOException;

/**
 * Created by jacob on 2/8/15.
 */

public class Pirate extends Api.Path {
    public Pirate(Api api) {
        super(api);
    }

    @Override
    public void call() throws IOException, PrintingError {
        Query query = Query.newBuilder().setOptions(QueryOptions.newBuilder().setLimit(1000).build()).build("distance(latlng, geopoint(0, 0)) > 0");
        Results<ScoredDocument> results = Search.getService().index.get(Search.Type.BOUNTY).search(query);

        for (ScoredDocument doc : results) {
            Search.getService().index.get(Search.Type.BOUNTY).delete(doc.getId());
        }

        response.getWriter().write("yarr!");
    }
}