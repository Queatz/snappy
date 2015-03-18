package com.queatz.snappy.thing;

import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Field;
import com.google.appengine.api.search.GeoPoint;
import com.google.appengine.api.search.PutException;
import com.google.appengine.api.search.PutResponse;
import com.google.appengine.api.search.Results;
import com.google.appengine.api.search.ScoredDocument;
import com.queatz.snappy.service.Config;
import com.queatz.snappy.service.Search;
import com.queatz.snappy.service.Things;
import com.queatz.snappy.service.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by jacob on 2/15/15.
 */
public class Party implements Thing {
    public Things things;

    public Party(Things t) {
        things = t;
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
            o.put("location", things.location.toJson(things.snappy.search.get(Search.Type.LOCATION, d.getOnlyField("location").getAtom()), user, true));
            o.put("host", things.person.toJson(things.snappy.search.get(Search.Type.PERSON, host), user, true));
            o.put("date", Util.dateToString(d.getOnlyField("date").getDate()));
            o.put("name", d.getOnlyField("name").getAtom());

            if(shallow)
                return o;

            if(isHost) {
                try {
                    o.put("full", Boolean.valueOf(d.getOnlyField("full").getAtom()));
                }
                catch (IllegalArgumentException ignored) {}
            }

            Results<ScoredDocument> results = things.snappy.search.index.get(Search.Type.JOIN).search("party = " + party);

            if(results.getNumberReturned() > 0) {
                JSONArray people = new JSONArray();

                for (ScoredDocument document : results) {
                    if(isHost || (user != null && user.equals(document.getOnlyField("person").getAtom())) || Config.JOIN_STATUS_IN.equals(document.getOnlyField("status").getAtom()))
                        people.put(things.join.toJson(document, user, false));
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
                locationDocument = things.location.createFromJson(req, user, new JSONObject(location));
                location = locationDocument.getId();
            }
            catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }

        if(locationDocument == null) {
            locationDocument = things.snappy.search.get(Search.Type.LOCATION, location);
        }

        GeoPoint loc = locationDocument.getOnlyField("location").getGeoPoint();

        Document.Builder documentBuild = Document.newBuilder();

        if(group != null)
            documentBuild.addField(Field.newBuilder().setName("group").setAtom(group));

        documentBuild.addField(Field.newBuilder().setName("name").setAtom(Util.encode(name)));
        documentBuild.addField(Field.newBuilder().setName("date").setDate(Util.stringToDate(date)));
        documentBuild.addField(Field.newBuilder().setName("location").setAtom(location));
        documentBuild.addField(Field.newBuilder().setName("loc_cache").setGeoPoint(loc));
        documentBuild.addField(Field.newBuilder().setName("details").setText(Util.encode(details)));
        documentBuild.addField(Field.newBuilder().setName("host").setAtom(user));
        documentBuild.addField(Field.newBuilder().setName("full").setAtom(Boolean.toString(false)));

        Document document = documentBuild.build();

        try {
            PutResponse put = things.snappy.search.index.get(Search.Type.PARTY).put(document);
            documentBuild.setId(put.getIds().get(0));
            return documentBuild.build();
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
            things.snappy.search.index.get(Search.Type.PARTY).put(document);
        } catch (PutException e) {
            e.printStackTrace();
        }
    }
}