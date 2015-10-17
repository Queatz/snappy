package com.queatz.snappy.backend;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

/**
 * Created by jacob on 10/17/15.
 */
public class StringClipper implements JsonSerializer<String> {
    @Override
    public JsonElement serialize(String src, Type typeOfSrc, JsonSerializationContext context) {
        return context.serialize(src.length() > 200 ? src.substring(0, 200) + "…" : src);
    }
}
