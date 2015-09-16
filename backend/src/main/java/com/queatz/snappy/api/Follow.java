package com.queatz.snappy.api;

import com.google.appengine.api.search.Document;
import com.queatz.snappy.backend.PrintingError;
import com.queatz.snappy.service.Api;
import com.queatz.snappy.service.Search;
import com.queatz.snappy.service.Things;

import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by jacob on 2/19/15.
 */
public class Follow extends Api.Path {
    public Follow(Api api) {
        super(api);
    }

    @Override
    public void call() throws IOException, PrintingError {
        switch (method) {
            case GET:
                if(path.size() != 1) {
                    die("follow - bad path");
                }

                getFollow(path.get(0));

                break;
            default:
                die("follow - bad method");
        }
    }

    private void getFollow(String followId) throws IOException, PrintingError {
        Document follow = Search.getService().get(Search.Type.FOLLOW, followId);
        JSONObject r = Things.getService().follow.toJson(follow, user, false);

        if(r != null)
            response.getWriter().write(r.toString());
        else
            notFound();
    }
}
