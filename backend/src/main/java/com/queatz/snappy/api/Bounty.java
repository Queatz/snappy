package com.queatz.snappy.api;

import com.google.appengine.api.search.Document;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.queatz.snappy.backend.Config;
import com.queatz.snappy.backend.PrintingError;
import com.queatz.snappy.service.Api;
import com.queatz.snappy.service.Search;
import com.queatz.snappy.service.Things;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by jacob on 9/5/15.
 */
public class Bounty implements Api.Path {
    Api api;

    public Bounty(Api a) {
        api = a;
    }

    @Override
    public void call(ArrayList<String> path, String user, HTTPMethod method, HttpServletRequest req, HttpServletResponse resp) throws IOException, PrintingError {
        String bountyId;

        switch (method) {
            case POST:
                if(path.size() != 1)
                    throw new PrintingError(Api.Error.NOT_AUTHENTICATED, "bounty - bad path");

                bountyId = path.get(0);

                if(Boolean.valueOf(req.getParameter(Config.PARAM_CLAIM))) {
                    boolean claimed = Things.getService().bounty.claim(user, bountyId);

                    resp.getWriter().write(Boolean.toString(claimed));
                }

                break;
            case DELETE:
                if(path.size() != 1)
                    throw new PrintingError(Api.Error.NOT_AUTHENTICATED, "bounty - bad path");

                bountyId = path.get(0);

                Document bounty = Search.getService().get(Search.Type.BOUNTY, bountyId);

                if(bounty != null && user.equals(bounty.getOnlyField("poster").getAtom())) {
                    boolean success = Things.getService().bounty.delete(bounty);

                    resp.getWriter().write(Boolean.toString(success));
                }

                break;
            default:
                throw new PrintingError(Api.Error.NOT_AUTHENTICATED, "bounty - bad method");
        }
    }
}
