package com.queatz.snappy.service;

import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.queatz.snappy.shared.Config;

/**
 * Created by jacob on 8/27/16.
 */

public class ImageQueue {
    private static ImageQueue _service;

    public static ImageQueue getService() {
        if(_service == null)
            _service = new ImageQueue();

        return _service;
    }

    private com.google.appengine.api.taskqueue.Queue queue;

    public ImageQueue() {
        queue = QueueFactory.getQueue(Config.QUEUE_IMAGE_WORKER_NAME);
    }

    public void enqueue(String thing) {
        queue.add(TaskOptions.Builder.withUrl(Config.QUEUE_IMAGE_WORKER_URL)
                .param("thing", thing));
    }
}