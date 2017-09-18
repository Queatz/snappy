package com.queatz.snappy.chat;

/**
 * Created by jacob on 9/17/17.
 */

public class ChatAuthor {

    private String topic;
    private String avatar;

    public String getTopic() {
        return topic;
    }

    public ChatAuthor setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public String getAvatar() {
        return avatar;
    }

    public ChatAuthor setAvatar(String avatar) {
        this.avatar = avatar;
        return this;
    }
}
