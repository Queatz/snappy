package com.queatz.snappy.chat.actions;

import com.queatz.snappy.chat.ChatSession;

/**
 * Created by jacob on 8/9/17.
 */

public class MessageSend implements ChatMessage {
    private String topic;
    private String message;

    public String getTopic() {
        return topic;
    }

    public MessageSend setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public MessageSend setMessage(String message) {
        this.message = message;
        return this;
    }

    @Override
    public void got(ChatSession chat) {
        chat.getChat().broadcast(chat, this);
    }
}
