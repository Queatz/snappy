package com.queatz.snappy.logic;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.text.DateFormat;

/**
 * Created by jacob on 4/2/16.
 */
public class EarthJson {

    private final static Gson gson = new GsonBuilder()
            .setDateFormat(DateFormat.LONG, DateFormat.LONG)
            .create();

    public String toJson(Object object) {
        return gson.toJson(object);
    }
}