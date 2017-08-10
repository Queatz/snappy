package com.queatz.snappy.chat.actions;

import com.queatz.snappy.chat.ChatKind;
import com.queatz.snappy.chat.ChatSession;
import com.queatz.snappy.chat.ChatWorld;
import com.queatz.snappy.logic.EarthField;

import java.util.Date;

/**
 * Created by jacob on 8/9/17.
 */

public class AdAdd implements ChatMessage {

    private String name;
    private String topic;
    private Date date;
    private String description;

    public String getName() {
        return name;
    }

    public AdAdd setName(String name) {
        this.name = name;
        return this;
    }

    public String getTopic() {
        return topic;
    }

    public AdAdd setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public AdAdd setDescription(String description) {
        this.description = description;
        return this;
    }

    public Date getDate() {
        return date;
    }

    public AdAdd setDate(Date date) {
        this.date = date;
        return this;
    }

    @Override
    public void got(ChatSession chat) {
        ChatWorld world = chat.getChat().getWorld();

        world.add(world.stage(ChatKind.AD_KIND)
                .set(EarthField.GEO, chat.getLocation())
                .set(EarthField.NAME, getName())
                .set(EarthField.ABOUT, getDescription())
                .set(EarthField.TOPIC, getTopic()));

        chat.getChat().broadcast(chat, this);
    }
}
