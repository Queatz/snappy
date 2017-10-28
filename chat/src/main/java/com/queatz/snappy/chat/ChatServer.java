package com.queatz.snappy.chat;

import com.queatz.snappy.events.EarthUpdate;
import com.queatz.snappy.shared.Config;

import java.io.IOException;
import java.util.logging.Logger;

import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * Created by jacob on 8/7/17.
 */

@ServerEndpoint(value = "/ws", configurator = ChatLogic.class)
public class ChatServer {

    private Session session;
    private ChatSession chatSession;

    static {
        EarthUpdate.register(Config.PUSH_ACTION_NEW_CHAT, ChatEvent.class);
    }

    @OnOpen
    public void onOpen(Session session, EndpointConfig endpointConfig) {
        Logger.getAnonymousLogger().info("WEBSOCKET (SESSION): " + session.getId());
        this.session = session;

        session.setMaxIdleTimeout(MINUTES.toMillis(5));

        this.chatSession = new ChatSession(
                session,
                (ChatLogic) endpointConfig.getUserProperties().get("chat")
        );
        session.getUserProperties().put("chat", chatSession);
        chatSession.join();
    }

    @OnClose
    public void onClose() {
        if (session.isOpen()) {
            try {
                session.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

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