package com.queatz.snappy.thing;

import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Field;
import com.google.appengine.api.search.PutException;
import com.google.appengine.api.search.PutResponse;
import com.google.appengine.api.search.Results;
import com.google.appengine.api.search.ScoredDocument;
import com.queatz.snappy.backend.Config;
import com.queatz.snappy.service.Search;
import com.queatz.snappy.service.Things;
import com.queatz.snappy.backend.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * Created by jacob on 2/16/15.
 */
public class Join implements Thing {
    public Things things;

    public Join(Things t) {
        things = t;
    }

    public JSONObject makePush(Document join) {
        if(join == null)
            return null;

        Document party = Search.getService().get(Search.Type.PARTY, join.getOnlyField("party").getAtom());
        Document person = Search.getService().get(Search.Type.PERSON, join.getOnlyField("person").getAtom());

        JSONObject push = new JSONObject();

        try {
            String action;

            if(Config.JOIN_STATUS_REQUESTED.equals(join.getOnlyField("status").getAtom())) {
                action = Config.PUSH_ACTION_JOIN_REQUEST;
                push.put("person", things.person.toPushJson(person));
            }
            else if(Config.JOIN_STATUS_IN.equals(join.getOnlyField("status").getAtom())) {
                action = Config.PUSH_ACTION_JOIN_ACCEPTED;
            }
            else
                return null;

            push.put("action", action);
            push.put("join", join.getId());
            push.put("party", things.party.toPushJson(party));

        }
        catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return push;
    }

    public JSONObject toJson(Document d, String user, boolean shallow) {
        if(d == null)
            return null;

        JSONObject o = new JSONObject();

        try {
            o.put("id", d.getId());
            o.put("person", things.person.toJson(Search.getService().get(Search.Type.PERSON, d.getOnlyField("person").getAtom()), user, true));
            o.put("party", things.party.toJson(Search.getService().get(Search.Type.PARTY, d.getOnlyField("party").getAtom()), user, true));
            o.put("status", d.getOnlyField("status").getAtom());

            return o;
        }
        catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Document createOrUpdate(String user, String party) {
        Document join = null;
        Results<ScoredDocument> results;
        results = Search.getService().index.get(Search.Type.JOIN).search("person = \"" + user + "\" AND party = \"" + party + "\"");

        Iterator<ScoredDocument> iterator = results.iterator();
        if(iterator.hasNext())
            join = iterator.next();

        if(join != null && !Config.JOIN_STATUS_WITHDRAWN.equals(join.getOnlyField("status").getAtom()))
            return join;

        Document.Builder documentBuild = Document.newBuilder();

        if(join != null) {
            documentBuild.setId(join.getId());
            documentBuild.addField(Field.newBuilder().setName("status").setAtom(Config.JOIN_STATUS_REQUESTED));
            Util.copyIn(documentBuild, join, "status");
        }
        else {
            documentBuild.addField(Field.newBuilder().setName("person").setAtom(user));
            documentBuild.addField(Field.newBuilder().setName("party").setAtom(party));
            documentBuild.addField(Field.newBuilder().setName("status").setAtom(Config.JOIN_STATUS_REQUESTED));
        }

        Document document = documentBuild.build();

        try {
            PutResponse put = Search.getService().index.get(Search.Type.JOIN).put(document);
            documentBuild.setId(put.getIds().get(0));
            return documentBuild.build();
        } catch (PutException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean delete(String user, String party) {
        Document join = null;
        Results<ScoredDocument> results;
        results = Search.getService().index.get(Search.Type.JOIN).search("person = \"" + user + "\" AND party = \"" + party + "\"");

        Iterator<ScoredDocument> iterator = results.iterator();
        if(iterator.hasNext())
            join = iterator.next();

        if(join == null)
            return false;

        Document.Builder documentBuild = Document.newBuilder();
        documentBuild.setId(join.getId());
        documentBuild.addField(Field.newBuilder().setName("status").setAtom(Config.JOIN_STATUS_WITHDRAWN));

        Util.copyIn(documentBuild, join, "status");

        Document document = documentBuild.build();

        try {
            Search.getService().index.get(Search.Type.JOIN).put(document);
        } catch (PutException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public Document setStatus(Document join, String status) {
        Document.Builder documentBuild = Document.newBuilder()
                .setId(join.getId())
                .addField(Field.newBuilder().setName("status").setAtom(status));

        Util.copyIn(documentBuild, join, "status");

        Document document = documentBuild.build();

        try {
            Search.getService().index.get(Search.Type.JOIN).put(document);
        } catch (PutException e) {
            e.printStackTrace();
        }

        return document;
    }
}