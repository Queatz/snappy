package com.queatz.snappy.queue;

import com.queatz.snappy.util.HttpUtil;

import java.util.Map;

/**
 * Created by jacob on 4/5/17.
 */

public class QueueItem {
    private final String url;
    private final Map<String, String> params;

    public QueueItem(String url, Map<String, String> params) {
        this.url = url;
        this.params = params;
    }

    public String getUrl() {
        return url;
    }

    public Map<String, String> getParams() {
        return params;
    }

    @Override
    public String toString() {
        return url + " -> " + HttpUtil.mapToParametersString(params);
    }
}
