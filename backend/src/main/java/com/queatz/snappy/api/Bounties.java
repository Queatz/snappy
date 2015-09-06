package com.queatz.snappy.api;

import com.google.appengine.api.search.Document;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.queatz.snappy.backend.Config;
import com.queatz.snappy.backend.PrintingError;
import com.queatz.snappy.backend.Util;
import com.queatz.snappy.service.Api;
import com.queatz.snappy.service.Things;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by jacob on 9/5/15.
 */
public class Bounties implements Api.Path {
    Api api;

    public Bounties(Api a) {
        api = a;
    }

    @Override
    public void call(ArrayList<String> path, String user, HTTPMethod method, HttpServletRequest req, HttpServletResponse resp) throws IOException, PrintingError {
        switch (method) {
            case POST:
                if(path.size() != 0)
                    throw new PrintingError(Api.Error.NOT_AUTHENTICATED, "bounties - bad path");

                String localId = req.getParameter(Config.PARAM_LOCAL_ID);
                String details = req.getParameter(Config.PARAM_DETAILS);
                int price = 0;

                try {
                    price = Integer.parseInt(req.getParameter(Config.PARAM_PRICE));
                }
                catch (NumberFormatException e) {
                    e.printStackTrace();
                }

                Document bounty = Things.getService().bounty.create(user, details, price);
                JSONObject r = Things.getService().bounty.toJson(bounty, user, false);
                Util.localId(r, localId);

                if(r != null)
                    resp.getWriter().write(r.toString());
                else
                    throw new PrintingError(Api.Error.NOT_FOUND);

                break;
            default:
                throw new PrintingError(Api.Error.NOT_AUTHENTICATED, "bounties - bad method");
        }
    }
}

