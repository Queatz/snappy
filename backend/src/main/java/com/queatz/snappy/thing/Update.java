package com.queatz.snappy.thing;

import com.google.appengine.api.search.Document;
import com.queatz.snappy.service.Things;

import org.json.JSONObject;

/**
 * Created by jacob on 2/15/15.
 */
public class Update implements Thing {
    public Things things;

    public Update(Things t) {
        things = t;
    }

    public JSONObject toJson(Document doc, String user, boolean shallow) {
        return null;
    }
}