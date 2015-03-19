package com.queatz.snappy.api;

import com.google.appengine.api.search.Document;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsInputChannel;
import com.queatz.snappy.service.Api;
import com.queatz.snappy.service.Config;
import com.queatz.snappy.service.PrintingError;
import com.queatz.snappy.service.Search;
import com.queatz.snappy.service.Util;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
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
        switch (method) {
            case GET:
                if(path.size() < 1)
                    throw new PrintingError(Api.Error.NOT_AUTHENTICATED, "party - bad path");

                if(path.size() == 1) {
                    resp.getWriter().write("party + " + path.get(0));
                }
                else if(path.size() == 2) {
                    if(Config.PATH_PHOTO.equals(path.get(1))) {
                        GcsFilename uptoId = new GcsFilename(api.mAppIdentityService.getDefaultGcsBucketName(), path.get(0));

                        GcsInputChannel input = api.mGCS.openReadChannel(uptoId, 0);
                        InputStream stream = Channels.newInputStream(input);

                        while (stream.available() > 0)
                            resp.getWriter().write(stream.read());

                        input.close();
                    }
                }

                break;
            case POST:
                if(path.size() != 1)
                    throw new PrintingError(Api.Error.NOT_AUTHENTICATED, "party - bad path");

                String partyId = path.get(0);

                if(Boolean.valueOf(req.getParameter(Config.PARAM_JOIN))) {
                    Document party = api.snappy.search.get(Search.Type.PARTY, partyId);

                    if(party != null) {
                        String localId = req.getParameter(Config.PARAM_LOCAL_ID);
                        Document join = api.snappy.things.join.createOrUpdate(user, partyId);

                        if(join != null) {
                            JSONObject response = api.snappy.things.join.toJson(join, user, false);
                            Util.localId(response, localId);

                            resp.getWriter().write(response.toString());

                            api.snappy.push.send(party.getOnlyField("host").getAtom(), api.snappy.things.join.makePush(join));
                        }
                    }
                }
                else if(Boolean.valueOf(req.getParameter(Config.PARAM_CANCEL_JOIN))) {
                    resp.getWriter().write(Boolean.toString(api.snappy.things.join.delete(user, partyId)));
                }
                else if(Boolean.valueOf(req.getParameter(Config.PARAM_FULL))) {
                    Document party = api.snappy.search.get(Search.Type.PARTY, partyId);
                    if(party == null || user == null || !user.equals(party.getOnlyField("host").getAtom())) {
                        resp.getWriter().write(Boolean.toString(false));
                    }

                    api.snappy.things.party.setFull(party);
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