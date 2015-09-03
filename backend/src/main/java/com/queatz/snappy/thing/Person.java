package com.queatz.snappy.thing;

import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Field;
import com.google.appengine.api.search.GeoPoint;
import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.PutException;
import com.google.appengine.api.search.PutResponse;
import com.google.appengine.api.search.Results;
import com.google.appengine.api.search.ScoredDocument;
import com.queatz.snappy.backend.Config;
import com.queatz.snappy.service.Search;
import com.queatz.snappy.service.Things;
import com.queatz.snappy.backend.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by jacob on 2/15/15.
 */
public class Person implements Thing {
    public Things things;

    public Person(Things t) {
        things = t;
    }

    public JSONObject toPushJson(Document d) {
        if(d == null)
            return null;

        JSONObject o = new JSONObject();

        try {
            o.put("id", d.getId());
            o.put("firstName", d.getOnlyField("firstName").getAtom());

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
            o.put("firstName", d.getOnlyField("firstName").getAtom());
            o.put("lastName", d.getOnlyField("lastName").getAtom());
            o.put("imageUrl", d.getOnlyField("imageUrl").getAtom());

            if(d.getId().equals(user)) {
                o.put("auth", d.getOnlyField("token").getAtom());
            }

            if(shallow)
                return o;

            long infoFollowers, infoFollowing, infoHosted;

            Index follow = Search.getService().index.get(Search.Type.FOLLOW);
            Index party = Search.getService().index.get(Search.Type.PARTY);

            infoFollowers = follow.search("following = \"" + d.getId() + "\"").getNumberFound();
            infoFollowing = follow.search("person = \"" + d.getId() + "\"").getNumberFound();
            infoHosted = party.search("host = \"" + d.getId() + "\"").getNumberFound();

            o.put("infoFollowers", infoFollowers);
            o.put("infoFollowing", infoFollowing);
            o.put("infoHosted", infoHosted);
            o.put("about", d.getOnlyField("about").getText());

            Results<ScoredDocument> results = Search.getService().index.get(Search.Type.FOLLOW).search("following = \"" + d.getId() + "\"");

            JSONArray r = new JSONArray();

            for(ScoredDocument doc : results) {
                r.put(Things.getService().follow.toJson(doc, user, true));
            }

            if(r.length() > 0) {
                o.put("followers", r);
            }

            results = Search.getService().index.get(Search.Type.UPDATE).search("person = \"" + d.getId() + "\"");

            r = new JSONArray();

            for(ScoredDocument doc : results) {
                r.put(Things.getService().update.toJson(doc, user, true));
            }

            if(r.length() > 0) {
                o.put("updates", r);
            }

            results = Search.getService().index.get(Search.Type.OFFER).search("person = \"" + d.getId() + "\"");

            r = new JSONArray();

            for(ScoredDocument doc : results) {
                r.put(Things.getService().offer.toJson(doc, user, true));
            }

            if(r.length() > 0) {
                o.put("offers", r);
            }

            return o;
        }
        catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean updateSubscription(Document person, String subscriptionId) {
        if(person == null) {
            return false;
        }

        Document.Builder documentBuild = Document.newBuilder();
        documentBuild.setId(person.getId());
        documentBuild.addField(Field.newBuilder().setName("subscription").setAtom(subscriptionId));

        Util.copyIn(documentBuild, person, "subscription");

        Document result = documentBuild.build();

        try {
            Search.getService().index.get(Search.Type.PERSON).put(result);
        } catch (PutException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public boolean updateAbout(Document person, String about) {

        Document.Builder documentBuild = Document.newBuilder();
        documentBuild.setId(person.getId());
        documentBuild.addField(Field.newBuilder().setName("about").setText(about));

        Util.copyIn(documentBuild, person, "about");

        Document result = documentBuild.build();

        try {
            Search.getService().index.get(Search.Type.PERSON).put(result);
        } catch (PutException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public boolean updateLocation(String user, double latitude, double longitude) {
        Document person = Search.getService().get(Search.Type.PERSON, user);

        if(person == null) {
            return false;
        }

        Document.Builder documentBuild = Document.newBuilder();
        documentBuild.setId(person.getId());
        documentBuild.addField(Field.newBuilder().setName("latlng").setGeoPoint(new GeoPoint(latitude, longitude)));
        documentBuild.addField(Field.newBuilder().setName("around").setDate(new Date()));

        Util.copyIn(documentBuild, person, "latlng", "around");

        Document result = documentBuild.build();

        try {
            Search.getService().index.get(Search.Type.PERSON).put(result);
        } catch (PutException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public Document createOrUpdateWithJson(Document document, JSONObject jsonObject) {
        Document.Builder documentBuild = Document.newBuilder();

        try {
            documentBuild.addField(Field.newBuilder().setName("email").setAtom(jsonObject.getString("email")));

            if(document == null)
                documentBuild.addField(Field.newBuilder().setName("token").setAtom(Util.genToken()));
            else
                documentBuild.addField(Field.newBuilder().setName("token").setAtom(document.getOnlyField("token").getAtom()));

            documentBuild.addField(Field.newBuilder().setName("gender").setAtom(jsonObject.getString("gender")));
            documentBuild.addField(Field.newBuilder().setName("firstName").setAtom(Util.encode(jsonObject.getString("firstName"))));
            documentBuild.addField(Field.newBuilder().setName("lastName").setAtom(Util.encode(jsonObject.getString("lastName"))));
            documentBuild.addField(Field.newBuilder().setName("imageUrl").setAtom(jsonObject.getString("imageUrl")));
            documentBuild.addField(Field.newBuilder().setName("googleId").setAtom(jsonObject.getString("googleId")));

            if(document == null || document.getOnlyField("about").getText() == null || document.getOnlyField("about").getText().trim().length() < 1) {
                documentBuild.addField(Field.newBuilder().setName("about").setText(Util.encode(jsonObject.getString("about"))));
            }
            else {
                documentBuild.addField(Field.newBuilder().setName("about").setText(document.getOnlyField("about").getText()));
            }

            boolean subscribed = false;

            if(jsonObject.has("subscription")) {
                documentBuild.addField(Field.newBuilder().setName("subscription").setAtom(jsonObject.getString("subscription")));
                subscribed = true;
            }
            else {
                if(document != null) {
                    try {
                        documentBuild.addField(Field.newBuilder().setName("subscription").setAtom(document.getOnlyField("subscription").getAtom()));
                        subscribed = true;
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            }

            if(!subscribed) {
                if(Config.IN_BETA) {
                    documentBuild.addField(Field.newBuilder().setName("subscription").setAtom(Config.HOSTING_BETATESTER));
                }
                else if(Config.PUBLIC_BUY) {
                    documentBuild.addField(Field.newBuilder().setName("subscription").setAtom(Config.HOSTING_ENABLED_AVAILABLE));
                }
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        if(document != null) {
            documentBuild.setId(document.getId());

            Util.copyIn(documentBuild, document, "email", "token", "gender", "firstName", "lastName", "imageUrl", "about", "googleId", "subscription");
        }

        Document result = documentBuild.build();

        try {
            PutResponse put = Search.getService().index.get(Search.Type.PERSON).put(result);

            if(document == null) {
                documentBuild.setId(put.getIds().get(0));
                result = documentBuild.build();
            }
        } catch (PutException e) {
            e.printStackTrace();
            return null;
        }

        return result;
    }
}