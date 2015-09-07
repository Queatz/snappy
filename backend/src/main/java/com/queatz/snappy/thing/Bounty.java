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

import java.util.Date;

/**
 * Created by jacob on 9/5/15.
 */
public class Bounty implements Thing {
    public Things things;

    public Bounty(Things t) {
        things = t;
    }

    public JSONObject makePush(Document bounty) {
        if(bounty == null)
            return null;

        Document people = Search.getService().get(Search.Type.PERSON, bounty.getOnlyField("people").getAtom());

        JSONObject push = new JSONObject();

        try {
            String action;

            if(Config.BOUNTY_STATUS_FINISHED.equals(bounty.getOnlyField("status").getAtom())) {
                action = Config.PUSH_ACTION_BOUNTY_FINISHED;
                push.put("people", things.person.toPushJson(people));
            }
            else
                return null;

            push.put("action", action);
            push.put("bounty", bounty.getId());

        }
        catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return push;
    }

    @Override
    public JSONObject toJson(Document d, String user, boolean shallow) {
        if(d == null)
            return null;

        JSONObject o = new JSONObject();

        try {
            o.put("id", d.getId());
            o.put("poster", things.person.toJson(Search.getService().get(Search.Type.PERSON, d.getOnlyField("poster").getAtom()), user, true));
            o.put("details", d.getOnlyField("details").getText());
            o.put("price", d.getOnlyField("price").getNumber().intValue());
            o.put("status", d.getOnlyField("status").getAtom());
            o.put("posted", Util.dateToString(d.getOnlyField("posted").getDate()));


            JSONArray people = new JSONArray();
            String person = d.getOnlyField("people").getText();

            if(person != null && !person.isEmpty()) {
                people.put(Things.getService().person.toJson(Search.getService().get(Search.Type.PERSON, person), user, true));

                o.put("people", people);
            }

            return o;
        }
        catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean claim(String user, String bountyId) {
        Document bounty = Search.getService().get(Search.Type.BOUNTY, bountyId);

        if(bounty == null || !Config.BOUNTY_STATUS_OPEN.equals(bounty.getOnlyField("status").getAtom())) {
            return false;
        }

        Document.Builder documentBuild = Document.newBuilder();
        documentBuild.setId(bounty.getId());
        documentBuild.addField(Field.newBuilder().setName("status").setAtom(Config.BOUNTY_STATUS_CLAIMED));
        documentBuild.addField(Field.newBuilder().setName("people").setText(user));

        Util.copyIn(documentBuild, bounty, "status", "people");

        Document result = documentBuild.build();

        try {
            Search.getService().index.get(Search.Type.BOUNTY).put(result);
        } catch (PutException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public boolean finish(String user, String bountyId) {
        Document bounty = Search.getService().get(Search.Type.BOUNTY, bountyId);

        if(bounty == null || !Config.BOUNTY_STATUS_CLAIMED.equals(bounty.getOnlyField("status").getAtom())) {
            return false;
        }

        if(!user.equals(bounty.getOnlyField("people").getText())) {
            return false;
        }

        Document.Builder documentBuild = Document.newBuilder();
        documentBuild.setId(bounty.getId());
        documentBuild.addField(Field.newBuilder().setName("status").setAtom(Config.BOUNTY_STATUS_FINISHED));

        Util.copyIn(documentBuild, bounty, "status");

        Document result = documentBuild.build();

        try {
            Search.getService().index.get(Search.Type.BOUNTY).put(result);
        } catch (PutException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public Document create(String user, String details, double price) {
        if(price < Config.BOUNTY_MIN_PRICE  || price > Config.BOUNTY_MAX_PRICE)
            return null;

        Document person = Search.getService().get(Search.Type.PERSON, user);

        Document.Builder documentBuild = Document.newBuilder();
        documentBuild.addField(Field.newBuilder().setName("details").setText(details));
        documentBuild.addField(Field.newBuilder().setName("status").setAtom(Config.BOUNTY_STATUS_OPEN));
        documentBuild.addField(Field.newBuilder().setName("price").setNumber(price));
        documentBuild.addField(Field.newBuilder().setName("poster").setAtom(user));
        documentBuild.addField(Field.newBuilder().setName("posted").setDate(new Date()));
        documentBuild.addField(Field.newBuilder().setName("people").setText(""));
        documentBuild.addField(Field.newBuilder().setName("latlng").setGeoPoint(person.getOnlyField("latlng").getGeoPoint()));

        Document document = documentBuild.build();

        try {
            PutResponse put = Search.getService().index.get(Search.Type.BOUNTY).put(document);
            documentBuild.setId(put.getIds().get(0));
            return documentBuild.build();
        } catch (PutException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean delete(Document bounty) {
        if(bounty == null || !Config.BOUNTY_STATUS_OPEN.equals(bounty.getOnlyField("status").getAtom())) {
            return false;
        }

        Search.getService().index.get(Search.Type.BOUNTY).delete(bounty.getId());

        return true;
    }
}
