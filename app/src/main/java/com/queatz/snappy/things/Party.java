package com.queatz.snappy.things;

import org.json.JSONObject;

/**
 * Created by jacob on 2/14/15.
 */
public class Party extends Thing {

    @Override
    public Party fromJSON(JSONObject o) {
        return this;
    }

    @Override
    public JSONObject toJSON() {
        return null;
    }
}
