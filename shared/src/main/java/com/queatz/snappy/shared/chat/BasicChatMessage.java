package com.queatz.snappy.shared.chat;

import com.google.gson.JsonElement;

/**
 * Created by jacob on 8/9/17.
 */

public class BasicChatMessage {
    private String action;
    private JsonElement data;

    public BasicChatMessage() {}

    public BasicChatMessage(String action, JsonElement data) {
        this.action = action;
        this.data = data;
    }

    public String getAction() {
        return action;
    }

    public BasicChatMessage setAction(String action) {
        this.action = action;
        return this;
    }

    public JsonElement getData() {
        return data;
    }

    public BasicChatMessage setData(JsonElement data) {
        this.data = data;
        return this;
    }
}
