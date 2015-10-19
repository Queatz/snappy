package com.queatz.snappy.shared;

/**
 * Created by jacob on 10/14/15.
 */
public class PushSpec<T> {
    public String action;
    public T body;

    public PushSpec() {

    }
    public PushSpec(String action, T body) {
        this.action = action;
        this.body = body;
    }
}
