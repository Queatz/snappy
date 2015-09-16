package com.queatz.snappy.api;

import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Results;
import com.google.appengine.api.search.ScoredDocument;
import com.queatz.snappy.backend.Config;
import com.queatz.snappy.backend.PrintingError;
import com.queatz.snappy.backend.Util;
import com.queatz.snappy.service.Api;
import com.queatz.snappy.service.Push;
import com.queatz.snappy.service.Search;
import com.queatz.snappy.service.Things;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by jacob on 2/14/15.
 */
public class People extends Api.Path {
    public People(Api api) {
        super(api);
    }

    @Override
    public void call() throws IOException, PrintingError {
        String personId;

        switch (method) {
            case GET:
                if(path.size() == 1) {
                    getPerson(path.get(0));
                }
                else if(path.size() == 2) {
                    personId = path.get(0);

                    boolean followers = false;

                    switch (path.get(1)) {
                        case Config.PATH_FOLLOWERS:
                            followers = true;
                        case Config.PATH_FOLLOWING:
                            getFollows(followers, personId);
                            break;
                        case Config.PATH_PARTIES:
                            getParties(personId);
                            break;
                        default:
                            die("people - bad path");
                    }
                }
                else {
                    die("people - bad path");
                }

                break;
            case POST:
                if(path.size() != 1) {
                    die("people - bad path");
                }

                personId = path.get(0);

                if(Boolean.valueOf(request.getParameter(Config.PARAM_SEEN))) {
                    postSeen(personId);
                }
                else if(Boolean.toString(true).equals(request.getParameter(Config.PARAM_FOLLOW))) {
                    postFollow(personId);
                }
                else if(Boolean.toString(false).equals(request.getParameter(Config.PARAM_FOLLOW))) {
                    postUnfollow(personId);
                }
                else {
                    String message = request.getParameter(Config.PARAM_MESSAGE);

                    if(message != null) {
                        postMessage(personId, message);
                    }
                    else {
                        die("people - bad path");
                    }
                }

                break;
            default:
                die("people - bad method");
        }
    }

    private void getPerson(String personId) throws IOException, PrintingError {
        Document person = Search.getService().get(Search.Type.PERSON, personId);
        JSONObject r = Things.getService().person.toJson(person, user, false);

        if (r != null) {
            response.getWriter().write(r.toString());
        } else {
                notFound();
        }
    }

    private void getFollows(boolean followers, String personId) throws IOException, PrintingError {
        Document person = Search.getService().get(Search.Type.PERSON, personId);

        if (person == null) {
            throw new PrintingError(Api.Error.NOT_FOUND);
        }

        JSONArray jsonArray = new JSONArray();

        Results<ScoredDocument> results = Search.getService().index.get(Search.Type.FOLLOW).search(
                (followers ? "following" : "person") + " = \"" + personId + "\""
        );

        for (ScoredDocument result : results) {
            JSONObject follower = Things.getService().follow.toJson(
                    result, user, false
            );

            jsonArray.put(follower);
        }

        response.getWriter().write(jsonArray.toString());
    }

    private void getParties(String personId) throws IOException, PrintingError {
        Document person = Search.getService().get(Search.Type.PERSON, personId);

        if (person == null) {
            notFound();
        }

        JSONArray jsonArray = new JSONArray();

        Results<ScoredDocument> results = Search.getService().index.get(Search.Type.PARTY).search(
                "host = \"" + personId + "\""
        );

        for (ScoredDocument result : results) {
            JSONObject party = Things.getService().party.toJson(
                    result, user, true
            );

            jsonArray.put(party);
        }

        response.getWriter().write(jsonArray.toString());
    }

    private void postSeen(String personId) throws IOException {
        response.getWriter().write(Boolean.toString(Things.getService().contact.markSeen(user, personId)));
    }

    private void postFollow(String personId) throws IOException {
        Document person = Search.getService().get(Search.Type.PERSON, personId);

        String localId = request.getParameter(Config.PARAM_LOCAL_ID);

        if(person != null) {
            Document follow = Things.getService().follow.createOrUpdate(user, person.getId());

            if(follow != null) {
                JSONObject json = Things.getService().follow.toJson(follow, user, false);
                Util.localId(json, localId);

                response.getWriter().write(json.toString());

                Push.getService().send(follow.getOnlyField("following").getAtom(), Things.getService().follow.makePush(follow));
            }
        }
    }

    private void postUnfollow(String personId) {
        Document person = Search.getService().get(Search.Type.PERSON, personId);

        if(person != null) {
            Document follow = Things.getService().follow.get(user, person.getId());

            if(follow != null) {
                Things.getService().follow.stopFollowing(follow);
            }
        }
    }

    private void postMessage(String personId, String message) throws IOException {
        Document person = Search.getService().get(Search.Type.PERSON, personId);
        String localId = request.getParameter(Config.PARAM_LOCAL_ID);
        Document sent = Things.getService().message.newMessage(user, person.getId(), message);

        if(sent != null) {
            JSONObject json = Things.getService().message.toJson(sent, user, false);
            Util.localId(json, localId);

            response.getWriter().write(json.toString());

            Push.getService().send(sent.getOnlyField("to").getAtom(), Things.getService().message.makePush(sent));
        }
    }
}
