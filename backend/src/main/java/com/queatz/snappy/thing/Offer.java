package com.queatz.snappy.thing;

import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Field;
import com.google.appengine.api.search.PutException;
import com.google.appengine.api.search.PutResponse;
import com.queatz.snappy.backend.Config;
import com.queatz.snappy.service.Search;
import com.queatz.snappy.service.Things;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by jacob on 8/29/15.
 */
public class Offer implements Thing {
    @Override
    public JSONObject toJson(Document d, String user, boolean shallow) {
        if(d == null)
            return null;

        JSONObject o = new JSONObject();

        try {
            o.put("id", d.getId());
            o.put("person", Things.getService().person.toJson(Search.getService().get(Search.Type.PERSON, d.getOnlyField("person").getAtom()), user, true));
            o.put("details", d.getOnlyField("details").getText());
            o.put("price", d.getOnlyField("price").getNumber().intValue());

            return o;
        }
        catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Document create(String user, String details, double price) {
        if(price < 0 || price > Config.OFFER_MAX_PRICE)
            return null;

        Document.Builder documentBuild = Document.newBuilder();
        documentBuild.addField(Field.newBuilder().setName("details").setText(details));
        documentBuild.addField(Field.newBuilder().setName("person").setAtom(user));
        documentBuild.addField(Field.newBuilder().setName("price").setNumber(price));
        documentBuild.addField(Field.newBuilder().setName("created").setDate(new Date()));

        Document document = documentBuild.build();

        try {
            PutResponse put = Search.getService().index.get(Search.Type.OFFER).put(document);
            documentBuild.setId(put.getIds().get(0));
            return documentBuild.build();
        } catch (PutException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void delete(Document offer) {
        Search.getService().index.get(Search.Type.OFFER).delete(offer.getId());
    }
}
