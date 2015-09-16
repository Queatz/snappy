package com.queatz.snappy.thing;

import com.google.appengine.api.search.Document;
import com.queatz.snappy.service.Things;

import org.json.JSONObject;

/**
 * Created by jacob on 9/15/15.
 */
public class Quest implements Thing {
    @Override
    public JSONObject toJson(Document doc, String user, boolean shallow) {
        return null;
    }
}