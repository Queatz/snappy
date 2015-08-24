package com.queatz.snappy.thing;

import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Field;
import com.google.appengine.api.search.PutException;
import com.google.appengine.api.search.Results;
import com.google.appengine.api.search.ScoredDocument;
import com.queatz.snappy.service.Searc
;
impo t com.queatz.snappy.service.Thing
;
impo t com.queatz.snappy.backend.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.Iterator;

/**
 * Created by jacob on 2/21/15.
 */
public class Contact implements Thing {
    public Things things;

    public Contact(Things t) {
        things = t;
    }

    public JSONObject toJson(Document d, String user, boolean shallow) {
        if(d == null)
            return null;

        JSONObject o = new JSONObject();

        try {
            o.put("id", d.getId());
            o.put("person", things.person.toJson(Search.getService().get(Search.Type.PERSON, d.getOnlyField("person").getAtom()), user, true));
            o.put("contact", things.person.toJson(Search.getService().get(Search.Type.PERSON, d.getOnlyField("contact").getAtom()), user, true));
            o.put("last", things.message.toJson(Search.getService().get(Search.Type.MESSAGE, d.getOnlyField("last").getAtom()), user, true));
            o.put("updated", Util.dateToString(d.getOnlyField("updated").getDate()));
            o.put("seen", d.getOnlyField("seen").getAtom());

            return o;
        }
        catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean markSeen(String user, String personId) {
        Document contact = null;
        Results<ScoredDocument> results;
        results = Search.getService().index.get(Search.Type.CONTACT).search("person = \"" + user + "\" AND contact = \"" + personId + "\"");

        Iterator<ScoredDocument> iterator = results.iterator();
        if(iterator.hasNext())
            contact = iterator.next();

        if(contact == null)
            return false;

        Document.Builder documentBuild = Document.newBuilder();
        documentBuild.setId(contact.getId());
        documentBuild.addField(Field.newBuilder().setName("seen").setAtom(Boolean.toString(true)));

        Util.copyIn(documentBuild, contact, "seen");

        Document document = documentBuild.build();

        try {
            Search.getService().index.get(Search.Type.CONTACT).put(document);
        } catch (PutException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public void updateWithMessage(Document message) {
        for(String fromTo[] : new String[][] {
                new String [] {
                        "from",
                        "to"
                },
                new String [] {
                        "to",
                        "from"
                },
        }) {
            Document.Builder documentBuild = Document.newBuilder();

            String p1 = message.getOnlyField(fromTo[0]).getAtom();
            String p2 = message.getOnlyField(fromTo[1]).getAtom();

            Document contact = null;
            Results<ScoredDocument> results;
            results = Search.getService().index.get(Search.Type.CONTACT).search("person = \"" + p1 + "\" AND contact = \"" + p2 + "\"");

            Iterator<ScoredDocument> iterator = results.iterator();
            if(iterator.hasNext())
                contact = iterator.next();

            if(contact != null)
                documentBuild.setId(contact.getId());

            documentBuild.addField(Field.newBuilder().setName("person").setAtom(p1));
            documentBuild.addField(Field.newBuilder().setName("contact").setAtom(p2));
            documentBuild.addField(Field.newBuilder().setName("last").setAtom(message.getId()));
            documentBuild.addField(Field.newBuilder().setName("updated").setDate(new Date()));
            documentBuild.addField(Field.newBuilder().setName("seen").setAtom(Boolean.toString("from".equals(fromTo[0]))));

            Document document = documentBuild.build();

            try {
                Search.getService().index.get(Search.Type.CONTACT).put(document);
            } catch (PutException e) {
                e.printStackTrace();
            }
        }
    }
}