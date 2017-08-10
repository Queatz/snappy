package com.queatz.snappy.logic;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import java.lang.reflect.Type;
import java.text.DateFormat;

/**
 * Created by jacob on 4/2/16.
 */
public class EarthJson {

    private final static Gson gson = new GsonBuilder()
            .setDateFormat(DateFormat.FULL, DateFormat.FULL)
            .create();

    public String toJson(Object object) {
        return gson.toJson(object);
    }

    public JsonElement toJsonTree(Object object) {
        return gson.toJsonTree(object);
    }

    public <T> T fromJson(String json, Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }

    public <T> T fromJson(JsonElement json, Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }

    public <T> T fromJson(String json, Type token) {
        return gson.fromJson(json, token);
    }
}
