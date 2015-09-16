package com.queatz.snappy.api;

import com.google.appengine.api.search.Document;
import com.queatz.snappy.backend.Config;
import com.queatz.snappy.backend.PrintingError;
import com.queatz.snappy.backend.Util;
import com.queatz.snappy.service.Api;
import com.queatz.snappy.service.Push;
import com.queatz.snappy.service.Search;
import com.queatz.snappy.service.Things;

import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by jacob on 2/8/15.
 */

public class Party extends Api.Path {
    public Party(Api api) {
        super(api);
    }

    @Override
    public void call() throws IOException, PrintingError {
        String partyId;

        switch (method) {
            case GET:
                if (path.size() == 1) {
                    get(path.get(0));
                } else {
                    die("party - bad path");
                }

                break;
            case POST:
                if (path.size() != 1) {
                    die("party - bad path");
                }

                partyId = path.get(0);

                if (Boolean.valueOf(request.getParameter(Config.PARAM_JOIN))) {
                    postJoin(partyId);
                } else if (Boolean.valueOf(request.getParameter(Config.PARAM_CANCEL_JOIN))) {
                    postCancelJoin(partyId);
                } else if (Boolean.valueOf(request.getParameter(Config.PARAM_FULL))) {
                    postFull(partyId);
                } else {
                    die("party - bad path");
                }

                break;
            default:
                die("party - bad method");
        }
    }

    private void get(String partyId) throws IOException, PrintingError {
        Document party = Search.getService().get(Search.Type.PARTY, partyId);
        JSONObject r = Things.getService().party.toJson(party, user, false);

        if (r == null) {
            notFound();
        }

        response.getWriter().write(r.toString());
    }

    private void postJoin(String partyId) throws IOException {
        Document party = Search.getService().get(Search.Type.PARTY, partyId);

        if (party != null) {
            String localId = request.getParameter(Config.PARAM_LOCAL_ID);
            Document join = Things.getService().join.create(user, partyId);

            if (join != null) {
                JSONObject json = Things.getService().join.toJson(join, user, false);
                Util.localId(json, localId);

                response.getWriter().write(json.toString());

                Push.getService().send(party.getOnlyField("host").getAtom(), Things.getService().join.makePush(join));
            }
        }
    }

    private void postCancelJoin(String partyId) throws IOException {
        response.getWriter().write(Boolean.toString(Things.getService().join.delete(user, partyId)));
    }

    private void postFull(String partyId) throws IOException {
        Document party = Search.getService().get(Search.Type.PARTY, partyId);

        if (party == null || user == null || !user.equals(party.getOnlyField("host").getAtom())) {
            response.getWriter().write(Boolean.toString(false));
        }

        Things.getService().party.setFull(party);

        response.getWriter().write(Boolean.toString(true));
    }
}