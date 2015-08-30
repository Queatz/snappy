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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

import javax.print.Doc;

/**
 * Created by jacob on 2/19/15.
 */
public class Follow implements Thing {
    public Things things;

    public Follow(Things t) {
        things = t;
    }

    public JSONObject makePush(Document follow) {
        if(follow == null)
            return null;

        Document person = Search.getService().get(Search.Type.PERSON, follow.getOnlyField("person").getAtom());

        JSONObject push = new JSONObject();

        try {
            push.put("action", Config.PUSH_ACTION_FOLLOW);
            push.put("person", things.person.toPushJson(person));
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
            o.put("following", things.person.toJson(Search.getService().get(Search.Type.PERSON, d.getOnlyField("following").getAtom()), user, true));

            return o;
        }
        catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void stopFollowing(Document follow) {
        Search.getService().index.get(Search.Type.FOLLOW).delete(follow.getId());
    }

    public Document get(String user, String following) {
        Document follow = null;

        Results<ScoredDocument> results;
        results = Search.getService().index.get(Search.Type.FOLLOW).search("person = \"" + user + "\" AND following = \"" + following + "\"");

        Iterator<ScoredDocument> iterator = results.iterator();

        if(iterator.hasNext())
            follow = iterator.next();

        return follow;
    }

    public Document createOrUpdate(String user, String following) {
        Document follow = null;
        Results<ScoredDocument> results;
        results = Search.getService().index.get(Search.Type.FOLLOW).search("person = \"" + user + "\" AND following = \"" + following + "\"");

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
            PutResponse put = Search.getService().index.get(Search.Type.FOLLOW).put(document);
            documentBuild.setId(put.getIds().get(0));
            return documentBuild.build();
        } catch (PutException e) {
            e.printStackTrace();
            return null;
        }
    }
}
