package com.queatz.snappy.util;

import android.net.Uri;

import com.google.gson.JsonObject;
import com.queatz.snappy.team.Thing;

import io.realm.DynamicRealmObject;

/**
 * Created by jacob on 8/14/16.
 */

public class Functions {

    public static String getLocationText(DynamicRealmObject location) {
        return location.getDate(Thing.LATITUDE) +
                "," +
                location.getDouble(Thing.LONGITUDE) +
                "(" +
                Uri.encode(location.getString(Thing.NAME))
                + ")";
    }

    public static JsonObject getLocationJson(DynamicRealmObject location) {
        JsonObject o = new JsonObject();
        o.addProperty("latitude", location.getDouble(Thing.LATITUDE));
        o.addProperty("longitude", location.getDouble(Thing.LONGITUDE));
        o.addProperty("name", location.getString(Thing.NAME));
        o.addProperty("address", location.getString(Thing.ADDRESS));
        return o;
    }

    public static String getFullName(DynamicRealmObject person) {
        return person.getString(Thing.FIRST_NAME) + " " + person.getString(Thing.LAST_NAME);
    }

    public static String getImageUrlForSize(DynamicRealmObject person, int size) {
        if(person.hasField(Thing.IMAGE_URL) ||
                person.getString(Thing.IMAGE_URL) == null ||
                person.getString(Thing.IMAGE_URL).isEmpty() ||
                !person.getString(Thing.IMAGE_URL).contains("="))
            return null;

        return person.getString(Thing.IMAGE_URL).split("=")[0] + "=" + size;
    }
}
