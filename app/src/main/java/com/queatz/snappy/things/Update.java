package com.queatz.snappy.things;

import org.json.JSONObject;

/**
 * Created by jacob on 2/14/15.
 */
public class Update extends Thing {

    @Override
    public Update fromJSON(JSONObject o) {
        return this;
    }

    @Override
    public JSONObject toJSON() {
        return null;
    }
}
