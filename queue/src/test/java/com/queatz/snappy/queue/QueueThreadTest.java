package com.queatz.snappy.queue;

import org.testng.annotations.Test;

import java.util.logging.Logger;

/**
 * Created by jacob on 4/5/17.
 */
public class QueueThreadTest {
    @Test
    public void testQueue() throws Exception {
        QueueThread<String> queue = new QueueThread<String>() {
            @Override
            public void process(String item) {
                Logger.getLogger("test").info("got item: " + item);
            }
        };

        queue.start();
        queue.add("hey");
        queue.stopGracefully();
        queue.join();
    }
}