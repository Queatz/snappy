package com.queatz.snappy.api;

import com.google.appengine.api.search.Document;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.queatz.snappy.service.Api;
im
ort co .queatz.snappy.backend.Config;
import co
.queat .snappy.backend.PrintingError;

mport  om.queatz.snappy.service.Push;
import com.queatz.snappy.service.Searc
;
impo t com.queatz.snappy.service.Thing
;
impo t com.queatz.snappy.backend.Util;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by jacob on 2/8/15.
 */

public class Party implements Api.Path {
    Api api;

    public Party(Api a) {
        api = a;
    }

    @Override
    public void call(ArrayList<String> path, String user, HTTPMethod method, HttpServletRequest req, HttpServletResponse resp) throws IOException, PrintingError {
        String partyId;
        Document party;

        switch (method) {
            case GET:
                if(path.size() < 1)
                    throw new PrintingError(Api.Error.NOT_AUTHENTICATED, "party - bad path");

                if(path.size() == 1) {
                    partyId = path.get(0);
                    party = Search.getService().get(Search.Type.PARTY, partyId);
                    JSONObject r = Things.getService().party.toJson(party, user, false);

                    if(r != null)
                        resp.getWriter().write(r.toString());
                    else
                        throw new PrintingError(Api.Error.NOT_FOUND);
                }
//                else if(path.size() == 2) {
//                    if(Config.PATH_PHOTO.equals(path.get(1))) {
//                        GcsFilename uptoId = new GcsFilename(api.mAppIdentityService.getDefaultGcsBucketName(), path.get(0));
//
//                        GcsInputChannel input = api.mGCS.openReadChannel(uptoId, 0);
//                        InputStream stream = Channels.newInputStream(input);
//
//                        while (stream.available() > 0)
//                            resp.getWriter().write(stream.read());
//
//                        input.close();
//                    }
//                }

                break;
            case POST:
                if(path.size() != 1)
                    throw new PrintingError(Api.Error.NOT_AUTHENTICATED, "party - bad path");

                partyId = path.get(0);

                if(Boolean.valueOf(req.getParameter(Config.PARAM_JOIN))) {
                    party = Search.getService().get(Search.Type.PARTY, partyId);

                    if(party != null) {
                        String localId = req.getParameter(Config.PARAM_LOCAL_ID);
                        Document join = Things.getService().join.createOrUpdate(user, partyId);

                        if(join != null) {
                            JSONObject response = Things.getService().join.toJson(join, user, false);
                            Util.localId(response, localId);

                            resp.getWriter().write(response.toString());

                            Push.getService().send(party.getOnlyField("host").getAtom(), Things.getService().join.makePush(join));
                        }
                    }
                }
                else if(Boolean.valueOf(req.getParameter(Config.PARAM_CANCEL_JOIN))) {
                    resp.getWriter().write(Boolean.toString(Things.getService().join.delete(user, partyId)));
                }
                else if(Boolean.valueOf(req.getParameter(Config.PARAM_FULL))) {
                    party = Search.getService().get(Search.Type.PARTY, partyId);
                    if(party == null || user == null || !user.equals(party.getOnlyField("host").getAtom())) {
                        resp.getWriter().write(Boolean.toString(false));
                    }

                    Things.getService().party.setFull(party);
                    resp.getWriter().write(Boolean.toString(true));
                }
                else {
                    throw new PrintingError(Api.Error.NOT_AUTHENTICATED, "party - bad path");
                }

                break;
            case DELETE:


                break;
            default:
                throw new PrintingError(Api.Error.NOT_AUTHENTICATED, "party - bad method");
        }
    }
}