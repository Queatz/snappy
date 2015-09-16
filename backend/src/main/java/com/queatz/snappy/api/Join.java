package com.queatz.snappy.api;

import com.google.appengine.api.search.Document;
import com.queatz.snappy.backend.Config;
import com.queatz.snappy.backend.PrintingError;
import com.queatz.snappy.service.Api;
import com.queatz.snappy.service.Push;
import com.queatz.snappy.service.Search;
import com.queatz.snappy.service.Things;

import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by jacob on 2/18/15.
 */
public class Join extends Api.Path {
    public Join(Api api) {
        super(api);
    }

    @Override
    public void call() throws IOException, PrintingError {
        switch (method) {
            case GET:
                if (path.size() != 1) {
                    die("people - bad path");
                }

                get(path.get(0));

                break;
            case POST:
                if (path.size() != 1) {
                    die("join - bad path");
                }

                if (Boolean.valueOf(request.getParameter(Config.PARAM_HIDE))) {
                    postHide(path.get(0));
                } else if (Boolean.valueOf(request.getParameter(Config.PARAM_ACCEPT))) {
                    postAccept(path.get(0));
                } else {
                    die("join - bad path");
                }

                break;
            default:
                die("join - bad method");
        }
    }

    private void get(String joinId) throws IOException, PrintingError {
        Document join = Search.getService().get(Search.Type.JOIN, joinId);
        JSONObject r = Things.getService().join.toJson(join, user, false);

        if (r != null) {
            response.getWriter().write(r.toString());
        } else {
            notFound();
        }
    }

    private void postHide(String joinId) throws IOException {
        boolean succeeded = false;
        Document join = Search.getService().get(Search.Type.JOIN, joinId);

        if (join != null && Config.JOIN_STATUS_REQUESTED.equals(join.getOnlyField("status").getAtom())) {
            Document party = Search.getService().get(Search.Type.PARTY, join.getOnlyField("party").getAtom());

            if (party != null) {
                if (user.equals(party.getOnlyField("host").getAtom())) {
                    Things.getService().join.setStatus(join, Config.JOIN_STATUS_OUT);
                    succeeded = true;
                }
            }
        }

        response.getWriter().write(Boolean.toString(succeeded));
    }

    private void postAccept(String joinId) throws IOException {
        boolean succeeded = false;
        Document join = Search.getService().get(Search.Type.JOIN, joinId);

        if (join != null && Config.JOIN_STATUS_REQUESTED.equals(join.getOnlyField("status").getAtom())) {
            Document party = Search.getService().get(Search.Type.PARTY, join.getOnlyField("party").getAtom());

            if (party != null) {
                if (user.equals(party.getOnlyField("host").getAtom())) {
                    join = Things.getService().join.setStatus(join, Config.JOIN_STATUS_IN);
                    succeeded = true;
                }
            }
        }

        response.getWriter().write(Boolean.toString(succeeded));

        if (succeeded) {
            Push.getService().send(join.getOnlyField("person").getAtom(), Things.getService().join.makePush(join));
        }
    }
}
