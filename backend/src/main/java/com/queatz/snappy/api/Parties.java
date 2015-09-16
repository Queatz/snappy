package com.queatz.snappy.api;

import com.google.appengine.api.search.Document;
import com.queatz.snappy.backend.Config;
import com.queatz.snappy.backend.PrintingError;
import com.queatz.snappy.backend.Util;
import com.queatz.snappy.service.Api;
import com.queatz.snappy.service.Buy;
import com.queatz.snappy.service.Push;
import com.queatz.snappy.service.Things;

import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by jacob on 9/15/15.
 */
public class Parties extends Api.Path {
    public Parties(Api api) {
        super(api);
    }

    @Override
    public void call() throws IOException, PrintingError {
        switch (method) {
            case POST:
                if(path.size() != 0) {
                    die("parties - bad path");
                }

                post();

                break;
            default:
                die("parties - bad method");
        }
    }

    private void post() throws IOException, PrintingError {
        if(!Buy.getService().valid(user))
            throw new PrintingError(Api.Error.NOT_FOUND, "parties - not bought");

        String localId = request.getParameter(Config.PARAM_LOCAL_ID);

        Document document = Things.getService().party.createFromRequest(request, user);

        if(document != null) {
            JSONObject json = Things.getService().party.toJson(document, user, false);
            Util.localId(json, localId);

            Push.getService().sendToFollowers(user, Things.getService().party.makePush(document));
            response.getWriter().write(json.toString());
        }
    }
}
