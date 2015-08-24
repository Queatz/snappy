package com.queatz.snappy.api;

import com.google.appengine.api.search.Document;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.queatz.snappy.service.Api;
im
ort co .queatz.snappy.backend.Config;
import co
.queat .snappy.backend.PrintingError;
import com.queatz.snappy.service.Push;
import com.queatz.snappy.service.Search;
import com.queatz.snappy.service.Things;

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
                join = Search.getService().get(Search.Type.JOIN, joinId);
                JSONObject r = Things.getService().join.toJson(join, user, false);

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
                    join = Search.getService().get(Search.Type.JOIN, joinId);

                    if(join != null && Config.JOIN_STATUS_REQUESTED.equals(join.getOnlyField("status").getAtom())) {
                        Document party = Search.getService().get(Search.Type.PARTY, join.getOnlyField("party").getAtom());

                        if(party != null) {
                            if(user.equals(party.getOnlyField("host").getAtom())) {
                                Things.getService().join.setStatus(join, Config.JOIN_STATUS_OUT);
                                succeeded = true;
                            }
                        }
                    }

                    resp.getWriter().write(Boolean.toString(succeeded));
                }
                else if(Boolean.valueOf(req.getParameter(Config.PARAM_ACCEPT))) {
                    join = Search.getService().get(Search.Type.JOIN, joinId);

                    if(join != null && Config.JOIN_STATUS_REQUESTED.equals(join.getOnlyField("status").getAtom())) {
                        Document party = Search.getService().get(Search.Type.PARTY, join.getOnlyField("party").getAtom());

                        if(party != null) {
                            if(user.equals(party.getOnlyField("host").getAtom())) {
                                join = Things.getService().join.setStatus(join, Config.JOIN_STATUS_IN);
                                succeeded = true;
                            }
                        }
                    }

                    resp.getWriter().write(Boolean.toString(succeeded));

                    if(succeeded) {
                        Push.getService().send(join.getOnlyField("person").getAtom(), Things.getService().join.makePush(join));
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
