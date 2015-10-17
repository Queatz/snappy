package com.queatz.snappy.shared;

/**
 * Created by jacob on 10/14/15.
 */
public class PushSpec {
    public String action;
    public Object body;

    public PushSpec() {

    }
    public PushSpec(String action, Object body) {
        this.action = action;
        this.body = body;
    }
}
