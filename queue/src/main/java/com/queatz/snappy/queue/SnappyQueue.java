package com.queatz.snappy.queue;

import java.util.Map;

/**
 * Call any endpoints async.
 */
public class SnappyQueue {

    private final UrlPostQueueThread queue = new UrlPostQueueThread();

    /**
     * Get a queue reference by name.
     * @param name The name
     */
    public SnappyQueue(String name) {
        // Config: ArangoDB Collection
        queue.start();
    }

    /**
     * Adds a task onto the queue to be processed async.
     * @param url The url to call
     * @param params The GET params of te url
     */
    public void add(String url, Map<String, String> params) {
        // Save url in ArangoDB
        // Ping queue service to check ArangoDB
        queue.add(url, params);
    }

    /**
     * Stop the queue
     */
    public void stop() {
        queue.stopGracefully();
    }

    // Todo: Make queue executor service
}
