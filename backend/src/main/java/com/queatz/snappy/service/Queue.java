package com.queatz.snappy.service;

import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.queatz.snappy.shared.Config;

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

    private com.google.appengine.api.taskqueue.Queue queue;

    public Queue() {
        queue = QueueFactory.getQueue(Config.QUEUE_WORKER_NAME);
    }

    public void enqueuePushMessageToUser(String toUser, String action, String message) {
        queue.add(TaskOptions.Builder.withUrl(Config.QUEUE_WORKER_URL)
                .param("action", action)
                .param("toUser", toUser)
                .param("message", message));
    }

    public void enqueuePushMessageFromUser(String fromUser, String action, String message) {
        queue.add(TaskOptions.Builder.withUrl(Config.QUEUE_WORKER_URL)
                .param("action", action)
                .param("fromUser", fromUser)
                .param("message", message));
    }
}