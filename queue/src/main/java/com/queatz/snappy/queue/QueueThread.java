package com.queatz.snappy.queue;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * Created by jacob on 4/5/17.
 */

public abstract class QueueThread<T> extends Thread {
    private final Queue<T> queue = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean alive = new AtomicBoolean(true);

    public abstract void process(T item);

    @Override
    public void run() {
        Logger.getLogger(UrlPostQueueThread.class.getSimpleName()).info("Queue started");
        while (alive.get()) {
            if (queue.isEmpty()) try {
                synchronized (queue) {
                    queue.wait();
                }
            } catch (InterruptedException ignored) {}

            while (!queue.isEmpty()) {
                T item = queue.remove();
                Logger.getLogger(UrlPostQueueThread.class.getSimpleName()).info("Queue processing: " + item);
                process(item);
            }
        }
        Logger.getLogger(UrlPostQueueThread.class.getSimpleName()).info("Queue stopped");
    }

    public void add(T item) {
        Logger.getLogger(UrlPostQueueThread.class.getSimpleName()).info("Queue add: " + item);

        synchronized (queue) {
            queue.add(item);
            queue.notify();
        }
    }

    public void stopGracefully() {
        alive.set(false);
    }
}
