package com.queatz.snappy.util;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

/**
 * Created by jacob on 10/18/15.
 */
public class Json {
    static public <T> T from(String json, Class<T> clazz) {
        return new GsonBuilder().create().fromJson(json, clazz);
    }

    static public <T> T from(JsonElement json, Class<T> clazz) {
        return new GsonBuilder().create().fromJson(json, clazz);
    }
}
