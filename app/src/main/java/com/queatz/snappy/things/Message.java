package com.queatz.snappy.things;

import org.json.JSONObject;

/**
 * Created by jacob on 2/14/15.
 */
public class Message extends Thing {

    @Override
    public Message fromJSON(JSONObject o) {
        return this;
    }

    @Override
    public JSONObject toJSON() {
        return null;
    }
}
