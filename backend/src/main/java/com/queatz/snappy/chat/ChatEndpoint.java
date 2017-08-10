package com.queatz.snappy.chat;

import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

/**
 * Created by jacob on 8/9/17.
 */

class ChatEndpoint extends ServerEndpointConfig.Configurator {
    @Override
    public void modifyHandshake(ServerEndpointConfig conf,
                                HandshakeRequest req,
                                HandshakeResponse resp) {
        conf.getUserProperties().put("chat", this);
    }
}
