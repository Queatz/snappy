package com.queatz.snappy.thing;

import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Field;
import com.google.appengine.api.search.PutException;
import com.google.appengine.api.search.PutResponse;
import com.google.appengine.api.search.Results;
import com.google.appengine.api.search.ScoredDocument;
import com.queatz.snappy.service.Config;
import com.queatz.snappy.service.Search;
import com.queatz.snappy.service.Things;
import com.queatz.snappy.service.Util;

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

    public JSONObject toJson(Document d, String user, boolean shallow) {
        if(d == null)
            return null;

        JSONObject o = new JSONObject();

        try {
            o.put("id", d.getId());
            o.put("person", things.person.toJson(things.snappy.search.get(Search.Type.PERSON, d.getOnlyField("person").getAtom()), user, true));
            o.put("party", things.party.toJson(things.snappy.search.get(Search.Type.PARTY, d.getOnlyField("party").getAtom()), user, true));
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
        results = things.snappy.search.index.get(Search.Type.JOIN).search("person = \"" + user + "\" AND party = \"" + party + "\"");

        Iterator<ScoredDocument> iterator = results.iterator();
        if(iterator.hasNext())
            join = iterator.next();

        if(join != null)
            return join;

        Document.Builder documentBuild = Document.newBuilder();

        documentBuild.addField(Field.newBuilder().setName("person").setAtom(user));
        documentBuild.addField(Field.newBuilder().setName("party").setAtom(party));
        documentBuild.addField(Field.newBuilder().setName("status").setAtom(Config.JOIN_STATUS_REQUESTED));

        Document document = documentBuild.build();

        try {
            PutResponse put = things.snappy.search.index.get(Search.Type.JOIN).put(document);
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
        results = things.snappy.search.index.get(Search.Type.JOIN).search("person = \"" + user + "\" AND party = \"" + party + "\"");

        Iterator<ScoredDocument> iterator = results.iterator();
        if(iterator.hasNext())
            join = iterator.next();

        if(join == null)
            return false;

        try {
            things.snappy.search.index.get(Search.Type.JOIN).delete(join.getId());
        } catch (PutException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public void setStatus(Document join, String status) {
        Document.Builder documentBuild = Document.newBuilder()
                .setId(join.getId())
                .addField(Field.newBuilder().setName("status").setAtom(status));

        Util.copyIn(documentBuild, join, "status");

        Document document = documentBuild.build();

        try {
            things.snappy.search.index.get(Search.Type.JOIN).put(document);
        } catch (PutException e) {
            e.printStackTrace();
        }
    }
}