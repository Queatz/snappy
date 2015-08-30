package com.queatz.snappy.api;

import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Results;
import com.google.appengine.api.search.ScoredDocument;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.queatz.snappy.service.Api;
import com.queatz.snappy.backend.Config;
import com.queatz.snappy.backend.PrintingError;
import com.queatz.snappy.service.Push;
import com.queatz.snappy.service.Search;
import com.queatz.snappy.service.Things;
import com.queatz.snappy.backend.Util;
import com.queatz.snappy.thing.Thing;

import org.json.JSONArray;
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
        Results<ScoredDocument> results;
        JSONArray jsonArray;

        switch (method) {
            case GET:
                if(path.size() == 1) {
                    personId = path.get(0);
                    person = Search.getService().get(Search.Type.PERSON, personId);
                    JSONObject r = Things.getService().person.toJson(person, user, false);

                    if (r != null)
                        resp.getWriter().write(r.toString());
                    else
                        throw new PrintingError(Api.Error.NOT_FOUND);
                }
                else if(path.size() == 2) {
                    personId = path.get(0);

                    boolean followers = false;

                    switch (path.get(1)) {
                        case Config.PATH_FOLLOWERS:
                            followers = true;
                        case Config.PATH_FOLLOWING:
                            person = Search.getService().get(Search.Type.PERSON, personId);

                            if (person == null) {
                                throw new PrintingError(Api.Error.NOT_FOUND);
                            }

                            jsonArray = new JSONArray();

                            results = Search.getService().index.get(Search.Type.FOLLOW).search(
                                    (followers ? "following" : "person") + " = \"" + personId + "\""
                            );

                            for (ScoredDocument result : results) {
                                JSONObject follower = Things.getService().follow.toJson(
                                        result, user, false
                                );

                                jsonArray.put(follower);
                            }

                            resp.getWriter().write(jsonArray.toString());

                            break;
                        case Config.PATH_PARTIES:
                            person = Search.getService().get(Search.Type.PERSON, personId);

                            if (person == null) {
                                throw new PrintingError(Api.Error.NOT_FOUND);
                            }

                            jsonArray = new JSONArray();

                            results = Search.getService().index.get(Search.Type.PARTY).search(
                                    "host = \"" + personId + "\""
                            );

                            for (ScoredDocument result : results) {
                                JSONObject party = Things.getService().party.toJson(
                                        result, user, true
                                );

                                jsonArray.put(party);
                            }

                            resp.getWriter().write(jsonArray.toString());

                            break;
                        default:
                            throw new PrintingError(Api.Error.NOT_AUTHENTICATED, "people - bad path");
                    }
                }
                else {
                    throw new PrintingError(Api.Error.NOT_AUTHENTICATED, "people - bad path");
                }

                break;
            case POST:
                if(path.size() != 1)
                    throw new PrintingError(Api.Error.NOT_AUTHENTICATED, "people - bad path");

                personId = path.get(0);
                person = Search.getService().get(Search.Type.PERSON, personId);

                if(Boolean.valueOf(req.getParameter(Config.PARAM_SEEN))) {
                    resp.getWriter().write(Boolean.toString(Things.getService().contact.markSeen(user, personId)));
                }
                else if(Boolean.toString(true).equals(req.getParameter(Config.PARAM_FOLLOW))) {
                    String localId = req.getParameter(Config.PARAM_LOCAL_ID);

                    if(person != null) {
                        Document follow = Things.getService().follow.createOrUpdate(user, person.getId());

                        if(follow != null) {
                            JSONObject response = Things.getService().follow.toJson(follow, user, false);
                            Util.localId(response, localId);

                            resp.getWriter().write(response.toString());

                            Push.getService().send(follow.getOnlyField("following").getAtom(), Things.getService().follow.makePush(follow));
                        }
                    }
                }
                else if(Boolean.toString(false).equals(req.getParameter(Config.PARAM_FOLLOW))) {
                    if(person != null) {
                        Document follow = Things.getService().follow.get(user, person.getId());

                        if(follow != null) {
                            Things.getService().follow.stopFollowing(follow);
                        }
                    }
                }
                else {
                    String message = req.getParameter(Config.PARAM_MESSAGE);
                    String localId = req.getParameter(Config.PARAM_LOCAL_ID);

                    if(message != null) {
                        Document sent = Things.getService().message.newMessage(user, person.getId(), message);

                        if(sent != null) {
                            JSONObject response = Things.getService().message.toJson(sent, user, false);
                            Util.localId(response, localId);

                            resp.getWriter().write(response.toString());

                            Push.getService().send(sent.getOnlyField("to").getAtom(), Things.getService().message.makePush(sent));
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
