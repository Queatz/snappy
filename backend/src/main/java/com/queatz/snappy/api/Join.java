package com.queatz.snappy.api;

import com.google.appengine.api.search.Document;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.queatz.snappy.service.Api;
import com.queatz.snappy.service.Config;
import com.queatz.snappy.service.PrintingError;
import com.queatz.snappy.service.Search;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by jacob on 2/18/15.
 */
public class Join implements Api.Path {
    Api api;

    public Join(Api a) {
        api = a;
    }

    @Override
    public void call(ArrayList<String> path, String user, HTTPMethod method, HttpServletRequest req, HttpServletResponse resp) throws IOException, PrintingError {
        String joinId;
        Document join;

        switch (method) {
            case GET:
                if(path.size() != 1)
                    throw new PrintingError(Api.Error.NOT_AUTHENTICATED, "people - bad path");

                joinId = path.get(0);
                join = api.snappy.search.get(Search.Type.JOIN, joinId);
                JSONObject r = api.snappy.things.join.toJson(join, user, false);

                if(r != null)
                    resp.getWriter().write(r.toString());
                else
                    throw new PrintingError(Api.Error.NOT_FOUND);

                break;
            case POST:
                if(path.size() != 1)
                    throw new PrintingError(Api.Error.NOT_AUTHENTICATED, "join - bad path");

                joinId = path.get(0);

                boolean succeeded = false;

                if(Boolean.valueOf(req.getParameter(Config.PARAM_HIDE))) {
                    join = api.snappy.search.get(Search.Type.JOIN, joinId);

                    if(join != null && Config.JOIN_STATUS_REQUESTED.equals(join.getOnlyField("status").getAtom())) {
                        Document party = api.snappy.search.get(Search.Type.PARTY, join.getOnlyField("party").getAtom());

                        if(party != null) {
                            if(user.equals(party.getOnlyField("host").getAtom())) {
                                api.snappy.things.join.setStatus(join, Config.JOIN_STATUS_OUT);
                                succeeded = true;
                            }
                        }
                    }

                    resp.getWriter().write(Boolean.toString(succeeded));
                }
                else if(Boolean.valueOf(req.getParameter(Config.PARAM_ACCEPT))) {
                    join = api.snappy.search.get(Search.Type.JOIN, joinId);

                    if(join != null && Config.JOIN_STATUS_REQUESTED.equals(join.getOnlyField("status").getAtom())) {
                        Document party = api.snappy.search.get(Search.Type.PARTY, join.getOnlyField("party").getAtom());

                        if(party != null) {
                            if(user.equals(party.getOnlyField("host").getAtom())) {
                                join = api.snappy.things.join.setStatus(join, Config.JOIN_STATUS_IN);
                                succeeded = true;
                            }
                        }
                    }

                    resp.getWriter().write(Boolean.toString(succeeded));

                    if(succeeded) {
                        api.snappy.push.send(join.getOnlyField("person").getAtom(), api.snappy.things.join.makePush(join));
                    }
                }
                else {
                    throw new PrintingError(Api.Error.NOT_AUTHENTICATED, "join - bad path");
                }

                break;
            default:
                throw new PrintingError(Api.Error.NOT_AUTHENTICATED, "join - bad method");
        }
    }
}
