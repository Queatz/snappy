package com.queatz.snappy.backend;

/**
 * Created by jacob on 10/1/17.
 */

public class StringResponse extends RuntimeException {
    private String string;

    public StringResponse(String string) {
        this.string = string;
    }

    public String getString() {
        return string;
    }
}
