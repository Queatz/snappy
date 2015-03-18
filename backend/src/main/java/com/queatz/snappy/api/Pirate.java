package com.queatz.snappy.api;

import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.GetIndexesRequest;
import com.google.appengine.api.search.GetRequest;
import com.google.appengine.api.search.GetResponse;
import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.SearchServiceFactory;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.queatz.snappy.service.Api;
import com.queatz.snappy.service.PrintingError;
import com.queatz.snappy.service.Search;

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
        try {
            for (Index idx : SearchServiceFactory.getSearchService().getIndexes(GetIndexesRequest.newBuilder().build())) {
                while (true) {
                    List<String> docIds = new ArrayList<>();
                    GetRequest request = GetRequest.newBuilder().setReturningIdsOnly(true).build();
                    GetResponse<Document> response = idx.getRange(request);
                    if (response.getResults().isEmpty()) {
                        break;
                    }
                    for (Document doc : response) {
                        docIds.add(doc.getId());
                    }
                    idx.delete(docIds);
                }

                idx.deleteSchema();

                resp.getWriter().println(idx.getName() + " cleared");
            }
        }
        catch (RuntimeException ignored) { }

        api.snappy.search.index.get(Search.Type.PERSON).delete();
        resp.getWriter().write("yarr!");
    }
}