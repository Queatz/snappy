package com.queatz.snappy.shared;

/**
 * Created by jacob on 10/15/15.
 */
public class ErrorResponseSpec {
    String error;
    String reason;

    public ErrorResponseSpec() {}

    public ErrorResponseSpec(String error, String reason) {
        this.error = error;
        this.reason = reason;
    }
}
