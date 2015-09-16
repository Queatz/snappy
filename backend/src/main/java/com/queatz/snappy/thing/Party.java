package com.queatz.snappy.thing;

import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Field;
import com.google.appengine.api.search.GeoPoint;
import com.google.appengine.api.search.PutException;
import com.google.appengine.api.search.PutResponse;
import com.google.appengine.api.search.Results;
import com.google.appengine.api.search.ScoredDocument;
import com.queatz.snappy.backend.Config;
import com.queatz.snappy.backend.Util;
import com.queatz.snappy.service.Search;
import com.queatz.snappy.service.Things;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by jacob on 2/15/15.
 */
public class Party implements Thing {
    public JSONObject makePush(Document party) {
        if(party == null)
            return null;

        Document person = Search.getService().get(Search.Type.PERSON, party.getOnlyField("host").getAtom());

        JSONObject push = new JSONObject();

        try {
            push.put("action", Config.PUSH_ACTION_NEW_PARTY);
            push.put("host", Things.getService().person.toPushJson(person));
            push.put("party", toPushJson(party));

        }
        catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return push;
    }

    public JSONObject toPushJson(Document d) {
        if(d == null)
            return null;

        JSONObject o = new JSONObject();

        try {
            o.put("id", d.getId());
            o.put("name", d.getOnlyField("name").getText());
            o.put("date", Util.dateToString(d.getOnlyField("date").getDate()));

            return o;
        }
        catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public JSONObject toJson(Document d, String user, boolean shallow) {
        if(d == null)
            return null;

        JSONObject o = new JSONObject();

        try {
            String host = d.getOnlyField("host").getAtom();
            boolean isHost = user != null && user.equals(host);
            String party = d.getId();

            o.put("id", party);
            o.put("details", d.getOnlyField("details").getText());
            o.put("location", Things.getService().location.toJson(Search.getService().get(Search.Type.LOCATION, d.getOnlyField("location").getAtom()), user, true));
            o.put("host", Things.getService().person.toJson(Search.getService().get(Search.Type.PERSON, host), user, true));
            o.put("date", Util.dateToString(d.getOnlyField("date").getDate()));
            o.put("name", d.getOnlyField("name").getText());

            if(shallow)
                return o;

            if(isHost) {
                try {
                    o.put("full", Boolean.valueOf(d.getOnlyField("full").getAtom()));
                }
                catch (IllegalArgumentException ignored) {}
            }

            Results<ScoredDocument> results = Search.getService().index.get(Search.Type.JOIN).search("party = " + party);

            if(results.getNumberReturned() > 0) {
                JSONArray people = new JSONArray();

                for (ScoredDocument document : results) {
                    if(isHost || (user != null && user.equals(document.getOnlyField("person").getAtom())) || Config.JOIN_STATUS_IN.equals(document.getOnlyField("status").getAtom()))
                        people.put(Things.getService().join.toJson(document, user, false));
                }

                if(people.length() > 0)
                    o.put("people", people);
            }

            return o;
        }
        catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Document createFromRequest(HttpServletRequest req, String user) {
        String group = req.getParameter(Config.PARAM_ID);
        String name = req.getParameter(Config.PARAM_NAME);
        String date = req.getParameter(Config.PARAM_DATE);
        String location = req.getParameter(Config.PARAM_LOCATION);
        String details = req.getParameter(Config.PARAM_DETAILS);

        Document locationDocument = null;

        if(location.startsWith("{")) {
            try {
                locationDocument = Things.getService().location.createFromJson(req, user, new JSONObject(location));
                location = locationDocument.getId();
            }
            catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }

        if(locationDocument == null) {
            locationDocument = Search.getService().get(Search.Type.LOCATION, location);
        }

        GeoPoint loc = locationDocument.getOnlyField("location").getGeoPoint();

        Document.Builder documentBuild = Document.newBuilder();

        if(group != null)
            documentBuild.addField(Field.newBuilder().setName("group").setAtom(group));

        documentBuild.addField(Field.newBuilder().setName("name").setText(Util.encode(name)));
        documentBuild.addField(Field.newBuilder().setName("date").setDate(Util.stringToDate(date)));
        documentBuild.addField(Field.newBuilder().setName("location").setAtom(location));
        documentBuild.addField(Field.newBuilder().setName("loc_cache").setGeoPoint(loc));
        documentBuild.addField(Field.newBuilder().setName("details").setText(Util.encode(details)));
        documentBuild.addField(Field.newBuilder().setName("host").setAtom(user));
        documentBuild.addField(Field.newBuilder().setName("full").setAtom(Boolean.toString(false)));

        Document document = documentBuild.build();

        try {
            PutResponse put = Search.getService().index.get(Search.Type.PARTY).put(document);
            documentBuild.setId(put.getIds().get(0));
            document = documentBuild.build();

            Things.getService().update.create(Config.UPDATE_ACTION_HOST_PARTY, user, document.getId());

            return document;
        } catch (PutException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setFull(Document party) {
        Document.Builder documentBuild = Document.newBuilder()
                .setId(party.getId())
                .addField(Field.newBuilder().setName("full").setAtom(Boolean.toString(true)));

        Util.copyIn(documentBuild, party, "full");

        Document document = documentBuild.build();

        try {
            Search.getService().index.get(Search.Type.PARTY).put(document);
        } catch (PutException e) {
            e.printStackTrace();
        }
    }
}
