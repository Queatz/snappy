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
        EarthJson json = new EarthJson();
        JsonObject config = json.fromJson(action.getString(EarthField.DATA), JsonObject.class);

        JsonArray changeNotificationUrls = config.getAsJsonArray(ActionConfig.DATA_FIELD_CHANGE_NOTIFICATION_URLS);

        if (value == null) {
            value = "";
        }

        String person = user == null ? "" : user.key().name();

        for (JsonElement j : changeNotificationUrls) {
            JsonObject c = j.getAsJsonObject();
            String url = parse(c.get(ActionConfig.DATA_FIELD_URL).getAsString(), value, person);

            String method = c.has(ActionConfig.DATA_FIELD_REQUEST_METHOD) ? c.get(ActionConfig.DATA_FIELD_REQUEST_METHOD).getAsString() : ActionConfig.DATA_FIELD_REQUEST_METHOD_GET;

            JsonObject params = new JsonObject();
            JsonArray array = c.getAsJsonArray(ActionConfig.DATA_FIELD_REQUEST_PARAMS);

            for (JsonElement p : array) {
                params.addProperty(p.getAsJsonObject().get("key").getAsString(), parse(p.getAsJsonObject().get("value").getAsString(), value, person));
            }

            String data = c.has(ActionConfig.DATA_FIELD_REQUEST_PARAMS) ? json.toJson(params) : "";

            queue.add(Config.QUEUE_ACTION_CHANGE_WORKER_URL, ImmutableMap.of(
                    Config.PARAM_URL, url,
                    Config.PARAM_DATA, data,
                    Config.PARAM_TYPE, method));
        }
    }

    private String parse(String string, String value, String person) {
        return string.replace(ActionConfig.URL_PARAM_VALUE, value)
                .replace(ActionConfig.URL_PARAM_PERSON, person);
    }

    public void stop() {
        queue.stop();
    }
}
