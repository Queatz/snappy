package com.queatz.snappy.thing;

import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Field;
import com.google.appengine.api.search.PutException;
import com.google.appengine.api.search.PutResponse;
import com.queatz.snappy.service.Search;
import com.queatz.snappy.service.Things;
import com.queatz.snappy.service.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by jacob on 2/15/15.
 */
public class Message implements Thing {
    public Things things;

    public Message(Things t) {
        things = t;
    }

    public JSONObject toJson(Document d, String user, boolean shallow) {
        if(d == null)
            return null;

        JSONObject o = new JSONObject();

        try {
            o.put("id", d.getId());
            o.put("from", things.person.toJson(things.snappy.search.get(Search.Type.PERSON, d.getOnlyField("from").getAtom()), user, true));
            o.put("to", things.person.toJson(things.snappy.search.get(Search.Type.PERSON, d.getOnlyField("to").getAtom()), user, true));
            o.put("message", d.getOnlyField("message").getText());
            o.put("date", Util.dateToString(d.getOnlyField("date").getDate()));

            return o;
        }
        catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Document newMessage(String from, String to, String message) {
        Document.Builder documentBuild = Document.newBuilder();

        documentBuild.addField(Field.newBuilder().setName("from").setAtom(from));
        documentBuild.addField(Field.newBuilder().setName("to").setAtom(to));
        documentBuild.addField(Field.newBuilder().setName("message").setText(Util.encode(message)));
        documentBuild.addField(Field.newBuilder().setName("date").setDate(new Date()));

        Document document = documentBuild.build();

        try {
            PutResponse put = things.snappy.search.index.get(Search.Type.MESSAGE).put(document);
            documentBuild.setId(put.getIds().get(0));
            document = documentBuild.build();
        } catch (PutException e) {
            e.printStackTrace();
            return null;
        }

        things.snappy.things.contact.updateWithMessage(document);

        return document;
    }
}