package com.queatz.snappy.shared;

/**
 * Created by jacob on 10/14/15.
 */
public class PushSpec<T> {
    public @Push String action;
    public @Push T body;

    public PushSpec() {

    }

    public PushSpec(String action) {
        this.action = action;
    }

    public PushSpec(String action, T body) {
        this.action = action;
        this.body = body;
    }
}
