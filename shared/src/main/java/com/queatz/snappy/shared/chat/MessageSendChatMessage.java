package com.queatz.snappy.shared.chat;

import java.util.Date;

/**
 * Created by jacob on 9/17/17.
 */

public class MessageSendChatMessage {
    private String topic;
    private String message;
    private String photo;
    private String avatar;
    private Date date;

    public String getTopic() {
        return topic;
    }

    public MessageSendChatMessage setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public MessageSendChatMessage setMessage(String message) {
        this.message = message;
        return this;
    }

    public String getPhoto() {
        return photo;
    }

    public MessageSendChatMessage setPhoto(String photo) {
        this.photo = photo;
        return this;
    }

    public String getAvatar() {
        return avatar;
    }

    public MessageSendChatMessage setAvatar(String avatar) {
        this.avatar = avatar;
        return this;
    }

    public Date getDate() {
        return date;
    }

    public MessageSendChatMessage setDate(Date date) {
        this.date = date;
        return this;
    }
}
