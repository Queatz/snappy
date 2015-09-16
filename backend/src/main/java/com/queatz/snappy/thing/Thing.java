package com.queatz.snappy.thing;

import com.google.appengine.api.search.Document;

import org.json.JSONObject;

/**
 * Created by jacob on 2/15/15.
 */
public interface Thing {
    JSONObject toJson(Document doc, String user, boolean shallow);
}
