package com.queatz.snappy.backend;

import java.util.Map;

/**
 * Created by jacob on 10/14/15.
 */
public class PushSpec {
    public String action;
    public Map body;

    public PushSpec() {

    }

    public PushSpec(String action) {
        this.action = action;
    }

    public PushSpec(String action, Map body) {
        this.action = action;
        this.body = body;
    }
}
