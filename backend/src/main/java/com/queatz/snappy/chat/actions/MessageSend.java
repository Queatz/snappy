package com.queatz.snappy.chat.actions;

import com.queatz.snappy.chat.ChatKind;
import com.queatz.snappy.chat.ChatSession;
import com.queatz.snappy.chat.ChatWorld;
import com.queatz.snappy.logic.EarthField;

/**
 * Created by jacob on 8/9/17.
 */

public class MessageSend implements ChatMessage {

    private String topic;
    private String message;
    private String photo;
    private String avatar;

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

    public String getPhoto() {
        return photo;
    }

    public MessageSend setPhoto(String photo) {
        this.photo = photo;
        return this;
    }

    public String getAvatar() {
        return avatar;
    }

    public MessageSend setAvatar(String avatar) {
        this.avatar = avatar;
        return this;
    }

    @Override
    public void got(ChatSession chat) {
        ChatWorld world = chat.getChat().getWorld();

        world.add(world.stage(ChatKind.MESSAGE_KIND)
                .set(EarthField.GEO, chat.getLocation())
                .set(EarthField.MESSAGE, getMessage())
                .set(EarthField.IMAGE_URL, getAvatar())
                .set(EarthField.TOPIC, getTopic()));

        chat.getChat().broadcast(chat, this);
    }
}
