package com.queatz.snappy.thing;

import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Field;
import com.google.appengine.api.search.GeoPoint;
import com.google.appengine.api.search.PutException;
import com.google.appengine.api.search.PutResponse;
import com.google.appengine.repackaged.com.google.type.LatLng;
import com.queatz.snappy.service.Search;
import com.queatz.snappy.service.Things;
import com.queatz.snappy.backend.Util;

import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by jacob on 2/15/15.
 */
public class Location implements Thing {
    public Things things;

    public Location(Things t) {
        things = t;
    }

    @Override
    public JSONObject toJson(Document d, String user, boolean shallow) {
        if(d == null)
            return null;

        JSONObject o = new JSONObject();

        try {
            o.put("id", d.getId());
            o.put("name", d.getOnlyField("name").getText());

            if(d.getFieldCount("address") == 1)
                o.put("address", d.getOnlyField("address").getText());

            o.put("latitude", d.getOnlyField("location").getGeoPoint().getLatitude());
            o.put("longitude", d.getOnlyField("location").getGeoPoint().getLongitude());

            return o;
        }
        catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public GeoPoint getGeoPoint(Document d) {
        return d.getOnlyField("location").getGeoPoint();
    }

    public Document createFromJson(HttpServletRequest req, String user, JSONObject jsonObject) {
        Document.Builder documentBuild = Document.newBuilder();

        try {
            documentBuild.addField(Field.newBuilder().setName("name").setText(Util.encode(jsonObject.getString("name"))));
            documentBuild.addField(Field.newBuilder().setName("location").setGeoPoint(
                    new GeoPoint(
                            jsonObject.getDouble("latitude"),
                            jsonObject.getDouble("longitude")
                    )
            ));

            if(jsonObject.has("address"))
                documentBuild.addField(Field.newBuilder().setName("address").setText(Util.encode(jsonObject.getString("address"))));
        }
        catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        Document document = documentBuild.build();

        try {
            PutResponse put = Search.getService().index.get(Search.Type.LOCATION).put(document);
            documentBuild.setId(put.getIds().get(0));
            return documentBuild.build();
        } catch (PutException e) {
            e.printStackTrace();
            return null;
        }
    }

    /*public Document createFromRequest(HttpServletRequest req, String user) {
        String location = req.getParameter(Config.PARAM_LOCATION);
        String address = req.getParameter(Config.PARAM_ADDRESS);

        Document.Builder documentBuild = Document.newBuilder();

        documentBuild.addField(Field.newBuilder().setName("name").setText(location));
        documentBuild.addField(Field.newBuilder().setName("address").setText(address));
        documentBuild.addField(Field.newBuilder().setName("latitude").setNumber(0));
        documentBuild.addField(Field.newBuilder().setName("longitude").setNumber(0));

        Document document = documentBuild.build();

        try {
            PutResponse put = Search.getService().index.get(Search.Type.LOCATION).put(document);
            documentBuild.setId(put.getIds().get(0));
            return documentBuild.build();
        } catch (PutException e) {
            e.printStackTrace();
            return null;
        }
    }*/
}