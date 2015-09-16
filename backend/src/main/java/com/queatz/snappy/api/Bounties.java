package com.queatz.snappy.api;

import com.google.appengine.api.search.Document;
import com.queatz.snappy.backend.Config;
import com.queatz.snappy.backend.PrintingError;
import com.queatz.snappy.backend.Util;
import com.queatz.snappy.service.Api;
import com.queatz.snappy.service.Things;

import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by jacob on 9/5/15.
 */
public class Bounties extends Api.Path {
    public Bounties(Api api) {
        super(api);
    }

    @Override
    public void call() throws IOException, PrintingError {
        switch (method) {
            case POST:
                if (path.size() != 0) {
                    die("bounties - bad path");
                }

                post();

                break;
            default:
                die("bounties - bad method");
        }
    }

    private void post() throws IOException, PrintingError {
        String localId = request.getParameter(Config.PARAM_LOCAL_ID);
        String details = request.getParameter(Config.PARAM_DETAILS);
        int price = 0;

        try {
            price = Integer.parseInt(request.getParameter(Config.PARAM_PRICE));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        Document bounty = Things.getService().bounty.create(user, details, price);
        JSONObject r = Things.getService().bounty.toJson(bounty, user, false);
        Util.localId(r, localId);

        if (r != null) {
            response.getWriter().write(r.toString());
        } else {
            notFound();
        }
    }
}

