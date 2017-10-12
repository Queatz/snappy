package com.village.things;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.queatz.earth.EarthField;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.events.Queue;
import com.queatz.snappy.queue.SnappyQueue;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.shared.EarthJson;

/**
 * Created by jacob on 10/11/17.
 */

public class ActionQueue extends Queue {
    private static ActionQueue _service;

    public static ActionQueue getService() {
        if(_service == null)
            _service = new ActionQueue();

        return _service;
    }

    private SnappyQueue queue;

    private ActionQueue() {
        queue = new SnappyQueue(Config.QUEUE_ACTION_CHANGE_WORKER_URL);
    }

    public void enqueue(EarthThing action, String value, EarthThing user) {
        JsonArray changeNotificationUrls = new EarthJson()
                .fromJson(action.getString(EarthField.DATA), JsonObject.class)
                .getAsJsonArray(ActionConfig.DATA_FIELD_CHANGE_NOTIFICATION_URLS);

        if (value == null) {
            value = "";
        }

        String person = user == null ? "" : user.key().name();

        for (JsonElement j : changeNotificationUrls) {
            String url = j.getAsString()
                    .replace(ActionConfig.URL_PARAM_VALUE, value)
                    .replace(ActionConfig.URL_PARAM_PERSON, person);

            queue.add(Config.QUEUE_ACTION_CHANGE_WORKER_URL, ImmutableMap.of(Config.PARAM_URL, url));
        }
    }

    public void stop() {
        queue.stop();
    }
}
