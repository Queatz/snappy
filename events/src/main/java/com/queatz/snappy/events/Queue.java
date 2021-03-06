package com.queatz.snappy.events;

import com.google.common.collect.ImmutableMap;
import com.queatz.snappy.queue.SnappyQueue;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.shared.EarthJson;
import com.queatz.snappy.shared.earth.EarthGeo;

/**
 * Created by jacob on 4/11/15.
 */
public class Queue {
    private static Queue _service;

    public static Queue getService() {
        if(_service == null)
            _service = new Queue();

        return _service;
    }

    private SnappyQueue queue;

    public Queue() {
        queue = new SnappyQueue(Config.QUEUE_WORKER_NAME);
    }

    public void enqueuePushMessageToUser(String toUser, String action, String message) {
        queue.add(Config.QUEUE_WORKER_URL, ImmutableMap.of(
                "action", action,
                "toUser", toUser,
                "message", message
        ));
    }

    public void enqueuePushMessageFromUser(String fromUser, String action, String message) {
        queue.add(Config.QUEUE_WORKER_URL, ImmutableMap.of(
                "action", action,
                "fromUser", fromUser,
                "message", message
        ));
    }

    public void enqueuePushMessageFromLocation(EarthGeo location, String action, String message) {
        queue.add(Config.QUEUE_WORKER_URL, ImmutableMap.of(
                "action", action,
                "location", new EarthJson().toJson(location),
                "message", message
        ));
    }

    public void stop() {
        queue.stop();
    }
}