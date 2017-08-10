package com.queatz.snappy.chat;

import com.queatz.snappy.chat.actions.ChatMessage;
import com.queatz.snappy.logic.EarthGeo;
import com.queatz.snappy.logic.EarthJson;

import java.io.IOException;
import java.util.logging.Logger;

import javax.websocket.Session;

/**
 * Created by jacob on 8/9/17.
 */

public class ChatSession {

    private Session session;
    private ChatLogic chat;
    private EarthJson json;
    private EarthGeo location;

    public ChatSession(Session session, ChatLogic chat) {
        this.session = session;
        this.chat = chat;
        this.json = new EarthJson();
        this.location = new EarthGeo();
    }

    public void join() {
        chat.join(this);
    }

    public void leave() {
        chat.leave(this);
    }

    public void got(String message) {
        ChatMessage chatMessage = ChatMessageConverter.convert(json.fromJson(message, BasicChatMessage.class));

        if (chatMessage == null) {
            Logger.getAnonymousLogger().warning("CHAT INVALID ACTION: " + message);
            return;
        }

        chatMessage.got(this);
    }

    public void got(byte[] data) {
        // photos, topic
    }

    public Session getSession() {
        return session;
    }

    public ChatLogic getChat() {
        return chat;
    }

    public void send(ChatMessage message) {
        try {
            session.getBasicRemote().sendText(json.toJson(ChatMessageConverter.convert(message)));
        } catch (IOException e) {
            e.printStackTrace();
            Logger.getAnonymousLogger().warning("CHAT SEND ERROR: " + e.getMessage());
        }
    }

    public void setLocation(EarthGeo location) {
        this.location = location;
    }

    public EarthGeo getLocation() {
        return location;
    }
}