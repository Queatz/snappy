package com.queatz.snappy.thing;

import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Field;
import com.google.appengine.api.search.PutException;
import com.google.appengine.api.search.PutResponse;
import com.queatz.snappy.backend.Config;
import com.queatz.snappy.backend.Util;
import com.queatz.snappy.service.Search;
import com.queatz.snappy.service.Things;

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


    public JSONObject makePush(Document message) {
        if(message == null)
            return null;

        JSONObject push = new JSONObject();

        try {
            push.put("action", Config.PUSH_ACTION_MESSAGE);
            push.put("message", toPushJson(message));
        }
        catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return push;
    }

    public JSONObject toPushJson(Document d) {
        if(d == null)
            return null;

        JSONObject o = new JSONObject();

        try {
            o.put("id", d.getId());
            o.put("from", things.person.toPushJson(Search.getService().get(Search.Type.PERSON, d.getOnlyField("from").getAtom())));

            String msg = d.getOnlyField("message").getText();

            if(msg.length() > 200)
                msg = msg.substring(200) + "…";

            o.put("message", msg);

            return o;
        }
        catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public JSONObject toJson(Document d, String user, boolean shallow) {
        if(d == null)
            return null;

        JSONObject o = new JSONObject();

        try {
            o.put("id", d.getId());
            o.put("from", things.person.toJson(Search.getService().get(Search.Type.PERSON, d.getOnlyField("from").getAtom()), user, true));
            o.put("to", things.person.toJson(Search.getService().get(Search.Type.PERSON, d.getOnlyField("to").getAtom()), user, true));
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
            PutResponse put = Search.getService().index.get(Search.Type.MESSAGE).put(document);
            documentBuild.setId(put.getIds().get(0));
            document = documentBuild.build();
        } catch (PutException e) {
            e.printStackTrace();
            return null;
        }

        Things.getService().contact.updateWithMessage(document);

        return document;
    }
}