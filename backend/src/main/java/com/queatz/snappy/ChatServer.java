package com.queatz.snappy;

import java.io.IOException;
import java.util.logging.Logger;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

/**
 * Created by jacob on 8/7/17.
 */

@ServerEndpoint(value = "/ws")
public class ChatServer {

    private Session session;

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        Logger.getAnonymousLogger().info("WEBSOCKET (SESSION): " + session.getId());
    }

    @OnClose
    public void onClose() {
        Logger.getAnonymousLogger().info("WEBSOCKET: END");

    }

    @OnMessage
    public void onMessage(String message) throws IOException {
        Logger.getAnonymousLogger().info("WEBSOCKET (MESSAGE): " + message);
        session.getBasicRemote().sendText(message + "!");
    }

    @OnMessage
    public void onData(byte[] data) throws IOException {
        Logger.getAnonymousLogger().info("WEBSOCKET (DATA): " + data);
    }

    @OnError
    public void onError(Throwable t) throws Throwable {
        t.printStackTrace();
    }

}