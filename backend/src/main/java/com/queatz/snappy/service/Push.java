package com.queatz.snappy.service;

import com.google.appengine.api.search.Index;
import com.queatz.snappy.SnappyServlet;

import java.util.HashMap;

/**
 * Created by jacob on 3/18/15.
 */
public class Push {

    public SnappyServlet snappy;

    public Push(SnappyServlet s) {
        snappy = s;
    }

    public String send(String user, String message) {
        return "messageId";
    }

    public void clear(String messageId) {
        // Sends a push to all devices with this message to clear it (user has handled it)
    }
}
