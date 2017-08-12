package com.queatz.snappy;

import com.queatz.snappy.chat.ChatLogic;
import com.queatz.snappy.chat.ChatSession;

import java.io.IOException;
import java.util.logging.Logger;

import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

/**
 * Created by jacob on 8/7/17.
 */

@ServerEndpoint(value = "/ws", configurator = ChatLogic.class)
public class ChatServer {

    private Session session;
    private ChatSession chatSession;

    @OnOpen
    public void onOpen(Session session, EndpointConfig endpointConfig) {
        Logger.getAnonymousLogger().info("WEBSOCKET (SESSION): " + session.getId());
        this.session = session;
        this.chatSession = new ChatSession(
                session,
                (ChatLogic) endpointConfig.getUserProperties().get("chat")
        );
        session.getUserProperties().put("chat", chatSession);
        chatSession.join();
    }

    @OnClose
    public void onClose() {
        Logger.getAnonymousLogger().info("WEBSOCKET: END");
        chatSession.leave();
    }

    @OnMessage
    public void onMessage(String message) throws IOException {
        Logger.getAnonymousLogger().info("WEBSOCKET (MESSAGE): " + message);
        chatSession.got(message);
    }

    @OnMessage
    public void onData(byte[] data) throws IOException {
        Logger.getAnonymousLogger().info("WEBSOCKET (DATA): " + data.length);
        chatSession.got(data);
    }

    @OnError
    public void onError(Throwable t) throws Throwable {
        Logger.getAnonymousLogger().info("WEBSOCKET (ERROR): " + t.getMessage());
        t.printStackTrace();
    }

}