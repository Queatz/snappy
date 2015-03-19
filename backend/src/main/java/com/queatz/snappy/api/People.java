package com.queatz.snappy.api;

import com.google.appengine.api.search.Document;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.queatz.snappy.service.Api;
import com.queatz.snappy.service.Config;
import com.queatz.snappy.service.PrintingError;
import com.queatz.snappy.service.Search;
import com.queatz.snappy.service.Util;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by jacob on 2/14/15.
 */
public class People implements Api.Path {
    Api api;

    public People(Api a) {
        api = a;
    }

    @Override
    public void call(ArrayList<String> path, String user, HTTPMethod method, HttpServletRequest req, HttpServletResponse resp) throws IOException, PrintingError {
        String personId;
        Document person;

        switch (method) {
            case GET:
                if(path.size() != 1)
                    throw new PrintingError(Api.Error.NOT_AUTHENTICATED, "people - bad path");

                personId = path.get(0);
                person = api.snappy.search.get(Search.Type.PERSON, personId);
                JSONObject r = api.snappy.things.person.toJson(person, user, false);

                if(r != null)
                    resp.getWriter().write(r.toString());
                else
                    throw new PrintingError(Api.Error.NOT_FOUND);

                break;
            case POST:
                if(path.size() != 1)
                    throw new PrintingError(Api.Error.NOT_AUTHENTICATED, "people - bad path");

                personId = path.get(0);
                person = api.snappy.search.get(Search.Type.PERSON, personId);

                if(Boolean.valueOf(req.getParameter(Config.PARAM_SEEN))) {
                    resp.getWriter().write(Boolean.toString(api.snappy.things.contact.markSeen(user, personId)));
                }
                else if(Boolean.valueOf(req.getParameter(Config.PARAM_FOLLOW))) {
                    String localId = req.getParameter(Config.PARAM_LOCAL_ID);

                    if(person != null) {
                        Document follow = api.snappy.things.follow.createOrUpdate(user, person.getId());

                        if(follow != null) {
                            JSONObject response = api.snappy.things.follow.toJson(follow, user, false);
                            Util.localId(response, localId);

                            resp.getWriter().write(response.toString());

                            api.snappy.push.send(follow.getOnlyField("following").getAtom(), api.snappy.things.follow.makePush(follow));
                        }
                    }
                }
                else {
                    String message = req.getParameter(Config.PARAM_MESSAGE);
                    String localId = req.getParameter(Config.PARAM_LOCAL_ID);

                    if(message != null) {
                        Document sent = api.snappy.things.message.newMessage(user, person.getId(), message);

                        if(sent != null) {
                            JSONObject response = api.snappy.things.message.toJson(sent, user, false);
                            Util.localId(response, localId);

                            resp.getWriter().write(response.toString());

                            api.snappy.push.send(sent.getOnlyField("to").getAtom(), api.snappy.things.message.makePush(sent));
                        }
                    }
                    else {
                        throw new PrintingError(Api.Error.NOT_AUTHENTICATED, "people - bad path");
                    }
                }

                break;
            case DELETE:

                break;
            default:
                throw new PrintingError(Api.Error.NOT_AUTHENTICATED, "people - bad method");
        }
    }
}
