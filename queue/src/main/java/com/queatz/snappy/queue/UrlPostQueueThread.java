package com.queatz.snappy.queue;

import com.queatz.snappy.util.HttpUtil;

import org.apache.http.entity.ContentType;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by jacob on 4/5/17.
 */

public class UrlPostQueueThread extends QueueThread<QueueItem> {

    @Override
    public void process(QueueItem item) {
        try {
            String response = HttpUtil.post(
                    item.getUrl(),
                    ContentType.APPLICATION_FORM_URLENCODED.toString(),
                    HttpUtil.mapToParameters(item.getParams()));
            Logger.getLogger(UrlPostQueueThread.class.getSimpleName()).info("Queue response: " + response);
        } catch (IOException e) {
            e.printStackTrace();
            Logger.getLogger(UrlPostQueueThread.class.getSimpleName()).info("Queue error: " + e);
        }
    }

    public void add(String item, Map<String, String> params) {
        add(new QueueItem(item, params));
    }
}
