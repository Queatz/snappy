package com.queatz.snappy.things;

import org.json.JSONObject;

/**
 * Created by jacob on 2/14/15.
 */
public abstract class Thing {
    public abstract Thing fromJSON(JSONObject o);
    public abstract JSONObject toJSON();
}
