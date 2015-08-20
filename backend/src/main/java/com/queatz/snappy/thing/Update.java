package com.queatz.snappy.thing;

import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Field;
import com.google.appengine.api.search.PutException;
import com.google.appengine.api.search.PutResponse;
import com.google.appengine.api.search.Results;
import com.google.appengine.api.search.ScoredDocument;
import com.queatz.snappy.backend.Config;
import com.queatz.snappy.backend.Util;
import com.queatz.snappy.service.Search;
import com.queatz.snappy.service.Things;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.Iterator;

/**
 * Created by jacob on 2/15/15.
 */
public class Update implements Thing {
    public Things things;

    public Update(Things t) {
        things = t;
    }

    public JSONObject toJson(Document d, String user, boolean shallow) {
        if(d == null)
            return null;

        JSONObject o = new JSONObject();

        try {
            o.put("id", d.getId());
            o.put("person", things.person.toJson(Search.getService().get(Search.Type.PERSON, d.getOnlyField("person").getAtom()), user, true));
            o.put("party", things.party.toJson(Search.getService().get(Search.Type.PARTY, d.getOnlyField("party").getAtom()), user, true));
            o.put("action", d.getOnlyField("action").getAtom());
            o.put("date", Util.dateToString(d.getOnlyField("date").getDate()));

            return o;
        }
        catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Document create(String action, String user, String partyId) {
        Document.Builder documentBuild = Document.newBuilder();
        documentBuild.addField(Field.newBuilder().setName("action").setAtom(action));
        documentBuild.addField(Field.newBuilder().setName("person").setAtom(user));
        documentBuild.addField(Field.newBuilder().setName("party").setAtom(partyId));
        documentBuild.addField(Field.newBuilder().setName("date").setDate(new Date()));

        Document document = documentBuild.build();

        try {
            PutResponse put = Search.getService().index.get(Search.Type.UPDATE).put(document);
            documentBuild.setId(put.getIds().get(0));
            return documentBuild.build();
        } catch (PutException e) {
            e.printStackTrace();
            return null;
        }
    }
}