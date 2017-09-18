package com.queatz.snappy.shared.chat;

import java.util.Date;

/**
 * Created by jacob on 9/17/17.
 */

public class AdAddChatMessage {

    private String name;
    private String topic;
    private Date date;
    private String description;
    private String source;

    private String token;

    public String getName() {
        return name;
    }

    public AdAddChatMessage setName(String name) {
        this.name = name;
        return this;
    }

    public String getTopic() {
        return topic;
    }

    public AdAddChatMessage setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public AdAddChatMessage setDescription(String description) {
        this.description = description;
        return this;
    }

    public Date getDate() {
        return date;
    }

    public AdAddChatMessage setDate(Date date) {
        this.date = date;
        return this;
    }

    public String getSource() {
        return source;
    }

    public AdAddChatMessage setSource(String source) {
        this.source = source;
        return this;
    }

    public String getToken() {
        return token;
    }

    public AdAddChatMessage setToken(String token) {
        this.token = token;
        return this;
    }
}
