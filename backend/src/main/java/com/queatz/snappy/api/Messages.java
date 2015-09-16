package com.queatz.snappy.api;

import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Query;
import com.google.appengine.api.search.QueryOptions;
import com.google.appengine.api.search.Results;
import com.google.appengine.api.search.ScoredDocument;
import com.queatz.snappy.backend.Config;
import com.queatz.snappy.backend.PrintingError;
import com.queatz.snappy.service.Api;
import com.queatz.snappy.service.Search;
import com.queatz.snappy.service.Things;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by jacob on 2/14/15.
 */

public class Messages extends Api.Path {
    public Messages(Api api) {
        super(api);
    }

    @Override
    public void call() throws IOException, PrintingError {
        switch (method) {
            case GET:
                if(path.size() == 0) {
                    get();
                } else if(path.size() == 1) {
                    get(path.get(0));
                } else {
                    die("messages - bad path");
                }

                break;
            default:
               die("messages - bad method");
        }
    }

    private void get() throws IOException {
        JSONObject r = new JSONObject();

        QueryOptions queryOptions = QueryOptions.newBuilder()
                .setLimit(Config.TEMPORARY_API_LIMIT)
                .build();

        Query query = Query.newBuilder().setOptions(queryOptions).build("from = \"" + user + "\" OR to = \"" + user + "\"");

        Results<ScoredDocument> results = Search.getService().index.get(Search.Type.MESSAGE).search(query);

        JSONArray a = new JSONArray();

        for(ScoredDocument doc : results) {
            a.put(Things.getService().message.toJson(doc, user, true));
        }

        results = Search.getService().index.get(Search.Type.CONTACT).search("person = \"" + user + "\"");

        JSONArray c = new JSONArray();

        for(ScoredDocument doc : results) {
            c.put(Things.getService().contact.toJson(doc, user, true));
        }

        try {
            if (a.length() > 0) {
                r.put("messages", a);
            }

            if (c.length() > 0) {
                r.put("contacts", c);
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        response.getWriter().write(r.toString());
    }

    private void get(String messageId) throws IOException, PrintingError {
        Document message = Search.getService().get(Search.Type.MESSAGE, messageId);

        JSONObject r = null;

        if(user.equals(message.getOnlyField("from").getAtom()) || user.equals(message.getOnlyField("to").getAtom())) {
            r = Things.getService().message.toJson(message, user, false);
        }

        if (r != null) {
            response.getWriter().write(r.toString());
        } else {
            notFound();
        }
    }
}

