package com.queatz.snappy.util;

import com.queatz.snappy.shared.SuccessResponseSpec;

/**
 * Created by jacob on 10/21/15.
 */
public class ResponseUtil {
    public static boolean isSuccess(String response) {
        return response != null && Boolean.valueOf(Json.from(response, SuccessResponseSpec.class).success);
    }
}
