package com.queatz.snappy.api;

import com.google.appengine.api.search.Results;
import com.google.appengine.api.search.ScoredDocument;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.queatz.snappy.service.Api;
import com.queatz.snappy.service.PrintingError;
import com.queatz.snappy.service.Search;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by jacob on 2/14/15.
 */

public class Messages implements Api.Path {
    Api api;

    public Messages(Api a) {
        api = a;
    }

    @Override
    public void call(ArrayList<String> path, String user, HTTPMethod method, HttpServletRequest req, HttpServletResponse resp) throws IOException, PrintingError {
        switch (method) {
            case GET:
                JSONObject r = new JSONObject();

                Results<ScoredDocument> results = api.snappy.search.index.get(Search.Type.MESSAGE).search("from = \"" + user + "\" OR to = \"" + user + "\"");

                JSONArray a = new JSONArray();

                for(ScoredDocument doc : results) {
                    a.put(api.snappy.things.message.toJson(doc, user, true));
                }

                results = api.snappy.search.index.get(Search.Type.CONTACT).search("person = \"" + user + "\"");

                JSONArray c = new JSONArray();

                for(ScoredDocument doc : results) {
                    c.put(api.snappy.things.contact.toJson(doc, user, true));
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

                resp.getWriter().write(r.toString());

                api.snappy.push.send(user, user, "Amanda is awesome");

                break;

            case POST:


                break;
            default:
                throw new PrintingError(Api.Error.NOT_AUTHENTICATED, "messages - bad method");
        }
    }
}

