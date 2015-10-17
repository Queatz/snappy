package com.queatz.snappy.backend;

/**
 * Created by jacob on 10/15/15.
 */
public class ObjectResponse extends RuntimeException {
    Object object;
    Json.Compression compression;

    public ObjectResponse(Object object) {
        this(object, Json.Compression.NONE);
    }

    public ObjectResponse(Object object, Json.Compression compression) {
        this.object = object;
        this.compression = compression;
    }

    public Object getObject() {
        return object;
    }

    public Json.Compression getCompression() {
        return compression;
    }
}
