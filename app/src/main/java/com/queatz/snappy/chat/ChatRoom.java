package com.queatz.snappy.chat;

import com.queatz.snappy.shared.chat.AdAddChatMessage;

import java.util.List;

/**
 * Created by jacob on 9/17/17.
 */

public class ChatRoom {
    private String name;
    private int recent;
    private List<AdAddChatMessage> ads;

    public String getName() {
        return name;
    }

    public ChatRoom setName(String name) {
        this.name = name;
        return this;
    }

    public int getRecent() {
        return recent;
    }

    public ChatRoom setRecent(int recent) {
        this.recent = recent;
        return this;
    }

    public List<AdAddChatMessage> getAds() {
        return ads;
    }

    public ChatRoom setAds(List<AdAddChatMessage> ads) {
        this.ads = ads;
        return this;
    }
}
