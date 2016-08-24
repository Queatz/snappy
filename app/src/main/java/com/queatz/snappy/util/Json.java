package com.queatz.snappy.util;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;

import java.text.DateFormat;

/**
 * Created by jacob on 10/18/15.
 */
public class Json {
    static public <T> T from(String json, Class<T> clazz) {
        try {
            return new GsonBuilder().setDateFormat(DateFormat.FULL, DateFormat.FULL).create().fromJson(json, clazz);
        } catch (JsonSyntaxException e) {
            return null;
        }
    }

    static public <T> T from(JsonElement json, Class<T> clazz) {
        try {
            return new GsonBuilder().setDateFormat(DateFormat.FULL, DateFormat.FULL).create().fromJson(json, clazz);
        } catch (JsonSyntaxException e) {
            return null;
        }
    }
}
