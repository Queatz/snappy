package com.queatz.snappy.backend;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

/**
 * Clips a string to max 200 chars.
 *
 * Created by jacob on 10/17/15.
 */
public class StringClipper implements JsonSerializer<String> {
    Gson gson = new GsonBuilder().create();

    @Override
    public JsonElement serialize(String src, Type typeOfSrc, JsonSerializationContext context) {

        return gson.toJsonTree(src.length() > 200 ? src.substring(0, 200) + "…" : src);
    }
}
