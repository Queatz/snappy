package com.queatz.snappy.service;

import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Field;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;

/**
 * Created by jacob on 2/16/15.
 */
public class Util {
    static DateFormat formatter = DateFormat.getDateTimeInstance(
            DateFormat.LONG,
            DateFormat.LONG,
            Locale.US);

    public static Date stringToDate(String date) {
        try {
            return formatter.parse(date);
        }
        catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String dateToString(Date date) {
        return formatter.format(date);
    }

    public static void localId(JSONObject o, String localId) {
        if(localId != null) try {
            o.put("localId", localId);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Helper function until Datastore has geo-spacial query support
    public static void copyIn(Document.Builder builder, Document document, String... ignore) {
        HashSet<String> ignored = new HashSet<>();
        Collections.addAll(ignored, ignore);

        for(Field field : document.getFields()) {
            if(!ignored.contains(field.getName()))
                builder.addField(field);
        }
    }

    // Helper function until Datastore has geo-spacial query support
    public static String encode(String string) {
        try {
            return URLEncoder.encode(string, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Helper function until Datastore has geo-spacial query support
    public static String decode(String string) {
        try {
            return URLDecoder.decode(string, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }
}
