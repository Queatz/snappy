package com.queatz.snappy.thing;

import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Field;
import com.google.appengine.api.search.PutException;
import com.google.appengine.api.search.PutResponse;
import com.google.appengine.api.search.Results;
import com.google.appengine.api.search.ScoredDocument;
import com.queatz.snappy.service.Search;
import com.queatz.snappy.service.Things;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * Created by jacob on 2/19/15.
 */
public class Follow implements Thing {
    public Things things;

    public Follow(Things t) {
        things = t;
    }

    public JSONObject toJson(Document d, String user, boolean shallow) {
        if(d == null)
            return null;

        JSONObject o = new JSONObject();

        try {
            o.put("id", d.getId());
            o.put("person", things.person.toJson(things.snappy.search.get(Search.Type.PERSON, d.getOnlyField("person").getAtom()), user, true));
            o.put("following", things.person.toJson(things.snappy.search.get(Search.Type.PERSON, d.getOnlyField("following").getAtom()), user, true));

            return o;
        }
        catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void obliterate(String id) {

    }

    public Document createOrUpdate(String user, String following) {
        Document follow = null;
        Results<ScoredDocument> results;
        results = things.snappy.search.index.get(Search.Type.FOLLOW).search("person = \"" + user + "\" AND following = \"" + following + "\"");

        Iterator<ScoredDocument> iterator = results.iterator();
        if(iterator.hasNext())
            follow = iterator.next();

        if(follow != null)
            return follow;

        Document.Builder documentBuild = Document.newBuilder();

        documentBuild.addField(Field.newBuilder().setName("person").setAtom(user));
        documentBuild.addField(Field.newBuilder().setName("following").setAtom(following));

        Document document = documentBuild.build();

        try {
            PutResponse put = things.snappy.search.index.get(Search.Type.FOLLOW).put(document);
            documentBuild.setId(put.getIds().get(0));
            return documentBuild.build();
        } catch (PutException e) {
            e.printStackTrace();
            return null;
        }
    }
}
