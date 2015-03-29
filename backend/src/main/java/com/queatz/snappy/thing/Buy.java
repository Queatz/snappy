package com.queatz.snappy.thing;

import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Field;
import com.google.appengine.api.search.PutException;
import com.google.appengine.api.search.PutResponse;
import com.google.appengine.api.search.Results;
import com.google.appengine.api.search.ScoredDocument;
import com.queatz.snappy.service.Search;
import com.queatz.snappy.service.Things;
import com.queatz.snappy.service.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.Iterator;

/**
 * Created by jacob on 3/28/15.
 */
public class Buy implements Thing {
    public Things things;

    public Buy(Things t) {
        things = t;
    }

    public JSONObject toJson(Document d, String user, boolean shallow) {
        return null;
    }

    public Document makeOrUpdate(String purchaseData, String subscriptionInfo) {
        JSONObject data, subscription;

        try {
            data = new JSONObject(purchaseData);
            subscription = new JSONObject(subscriptionInfo);

            Document purchase = null;
            Results<ScoredDocument> results;
            results = things.snappy.search.index.get(Search.Type.BUY).search("orderId = \"" + data.getString("orderId") + "\"");

            Iterator<ScoredDocument> iterator = results.iterator();
            if(iterator.hasNext())
                purchase = iterator.next();

            Document.Builder documentBuild = Document.newBuilder();

            if(purchase != null) {
                documentBuild.setId(purchase.getId());
                Util.copyIn(documentBuild, purchase, "startTimeMillis", "expiryTimeMillis", "autoRenewing");
            }
            else {
                documentBuild.addField(Field.newBuilder().setName("orderId").setAtom(data.getString("orderId")));
                documentBuild.addField(Field.newBuilder().setName("packageName").setAtom(data.getString("packageName")));
                documentBuild.addField(Field.newBuilder().setName("productId").setAtom(data.getString("productId")));
                documentBuild.addField(Field.newBuilder().setName("purchaseTime").setDate(Util.longToDate(data.getLong("purchaseTime"))));
                documentBuild.addField(Field.newBuilder().setName("purchaseState").setNumber(data.getLong("purchaseState")));
                documentBuild.addField(Field.newBuilder().setName("developerPayload").setAtom(data.getString("developerPayload")));
                documentBuild.addField(Field.newBuilder().setName("purchaseToken").setAtom(data.getString("purchaseToken")));
            }

            documentBuild.addField(Field.newBuilder().setName("startTimeMillis").setDate(Util.longToDate(subscription.getLong("startTimeMillis"))));
            documentBuild.addField(Field.newBuilder().setName("expiryTimeMillis").setDate(Util.longToDate(subscription.getLong("expiryTimeMillis"))));
            documentBuild.addField(Field.newBuilder().setName("autoRenewing").setAtom(Boolean.toString(subscription.getBoolean("autoRenewing"))));

            Document document = documentBuild.build();

            try {
                PutResponse put = things.snappy.search.index.get(Search.Type.BUY).put(document);
                if(purchase == null) {
                    documentBuild.setId(put.getIds().get(0));
                    return documentBuild.build();
                }
                else {
                    return document;
                }

            } catch (PutException e) {
                e.printStackTrace();
                return null;
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
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
            results = things.snappy.search.index.get(Search.Type.CONTACT).search("person = \"" + p1 + "\" AND contact = \"" + p2 + "\"");

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
                things.snappy.search.index.get(Search.Type.CONTACT).put(document);
            } catch (PutException e) {
                e.printStackTrace();
            }
        }
    }
}
