package com.queatz.snappy.api;

import com.google.appengine.api.search.Document;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.queatz.snappy.service.Api;
import com.queatz.snappy.service.PrintingError;
import com.queatz.snappy.service.Search;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by jacob on 2/19/15.
 */
public class Follow implements Api.Path {
    Api api;

    public Follow(Api a) {
        api = a;
    }

    @Override
    public void call(ArrayList<String> path, String user, HTTPMethod method, HttpServletRequest req, HttpServletResponse resp) throws IOException, PrintingError {
        String followId;
        Document follow;

        switch (method) {
            case GET:
                if(path.size() != 1)
                    throw new PrintingError(Api.Error.NOT_AUTHENTICATED, "people - bad path");

                followId = path.get(0);
                follow = api.snappy.search.get(Search.Type.FOLLOW, followId);
                JSONObject r = api.snappy.things.follow.toJson(follow, user, false);

                if(r != null)
                    resp.getWriter().write(r.toString());
                else
                    throw new PrintingError(Api.Error.NOT_FOUND);

                break;
            case POST:

                break;
            default:
                throw new PrintingError(Api.Error.NOT_AUTHENTICATED, "follow - bad method");
        }
    }
}
