package com.queatz.snappy.images;

import com.google.common.collect.ImmutableMap;
import com.queatz.snappy.queue.SnappyQueue;
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

    private SnappyQueue queue;

    public ImageQueue() {
        queue = new SnappyQueue(Config.QUEUE_IMAGE_WORKER_NAME);
    }

    public void enqueue(String thing) {
        queue.add(Config.QUEUE_IMAGE_WORKER_URL, ImmutableMap.of(
                Config.PARAM_THING, thing
        ));
    }

    public void stop() {
        queue.stop();
    }
}