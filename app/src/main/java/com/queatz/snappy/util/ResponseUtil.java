package com.queatz.snappy.util;

import com.google.gson.JsonObject;

/**
 * Created by jacob on 10/21/15.
 */
public class ResponseUtil {
    public static boolean isSuccess(String response) {
        return response != null && Json.from(response, JsonObject.class).get("success").getAsBoolean();
    }
}
