package com.queatz.snappy.thing;

import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Field;
import com.google.appengine.api.search.GeoPoint;
import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.PutException;
import com.google.appengine.api.search.PutResponse;
import com.google.appengine.api.search.Results;
import com.google.appengine.api.search.ScoredDocument;
import com.queatz.snappy.service.Search;
import com.queatz.snappy.service.Things;
import com.queatz.snappy.service.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by jacob on 2/15/15.
 */
public class Person implements Thing {
    public Things things;

    public Person(Things t) {
        things = t;
    }

    public JSONObject toJson(Document d, String user, boolean shallow) {
        if(d == null)
            return null;

        JSONObject o = new JSONObject();

        try {
            o.put("id", d.getId());
            o.put("firstName", d.getOnlyField("firstName").getAtom());
            o.put("lastName", d.getOnlyField("lastName").getAtom());
            o.put("imageUrl", d.getOnlyField("imageUrl").getAtom());

            if(shallow)
                return o;

            long infoFollowers, infoFollowing, infoHosted;

            Index follow = things.snappy.search.index.get(Search.Type.FOLLOW);
            Index party = things.snappy.search.index.get(Search.Type.PARTY);

            infoFollowers = follow.search("following = \"" + d.getId() + "\"").getNumberFound();
            infoFollowing = follow.search("person = \"" + d.getId() + "\"").getNumberFound();
            infoHosted = party.search("host = \"" + d.getId() + "\"").getNumberFound();

            o.put("infoFollowers", infoFollowers);
            o.put("infoFollowing", infoFollowing);
            o.put("infoHosted", infoHosted);

            Results<ScoredDocument> results = things.snappy.search.index.get(Search.Type.FOLLOW).search("following = \"" + d.getId() + "\"");

            JSONArray r = new JSONArray();

            for(ScoredDocument doc : results) {
                r.put(things.snappy.things.follow.toJson(doc, user, true));
            }

            if(r.length() > 0) {
                o.put("followers", r);
            }

            /*results = things.snappy.search.index.get(Search.Type.MESSAGE).search("from = \"" + d.getId() + "\" OR to = \"" + d.getId() + "\"");

            r = new JSONArray();

            for(ScoredDocument doc : results) {
                r.put(things.snappy.things.follow.toJson(doc, user, true));
            }

            if(r.length() > 0) {
                o.put("messages", r);
            }*/

            return o;
        }
        catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean updateLocation(String user, double latitude, double longitude) {
        Document person = things.snappy.search.get(Search.Type.PERSON, user);

        if(person == null) {
            return false;
        }

        Document.Builder documentBuild = Document.newBuilder();
        documentBuild.setId(person.getId());
        documentBuild.addField(Field.newBuilder().setName("latlng").setGeoPoint(new GeoPoint(latitude, longitude)));

        Util.copyIn(documentBuild, person, "latlng");

        Document result = documentBuild.build();

        try {
            things.snappy.search.index.get(Search.Type.PERSON).put(result);
        } catch (PutException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public Document createOrUpdateWithJson(Document document, JSONObject jsonObject) {
        Document.Builder documentBuild = Document.newBuilder();

        try {
            documentBuild.addField(Field.newBuilder().setName("email").setAtom(jsonObject.getString("email")));
            documentBuild.addField(Field.newBuilder().setName("token").setAtom(jsonObject.getString("token")));
            documentBuild.addField(Field.newBuilder().setName("gender").setAtom(jsonObject.getString("gender")));
            documentBuild.addField(Field.newBuilder().setName("firstName").setAtom(Util.encode(jsonObject.getString("firstName"))));
            documentBuild.addField(Field.newBuilder().setName("lastName").setAtom(Util.encode(jsonObject.getString("lastName"))));
            documentBuild.addField(Field.newBuilder().setName("imageUrl").setAtom(jsonObject.getString("imageUrl")));
            documentBuild.addField(Field.newBuilder().setName("about").setAtom(Util.encode(jsonObject.getString("about"))));
            documentBuild.addField(Field.newBuilder().setName("googleId").setAtom(jsonObject.getString("googleId")));
        }
        catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        if(document != null)
            documentBuild.setId(document.getId());

        Document result = documentBuild.build();

        try {
            PutResponse put = things.snappy.search.index.get(Search.Type.PERSON).put(result);

            if(document == null) {
                documentBuild.setId(put.getIds().get(0));
                result = documentBuild.build();
            }
        } catch (PutException e) {
            e.printStackTrace();
            return null;
        }

        return result;
    }
}