package com.queatz.snappy.chat.actions;

import com.queatz.snappy.chat.ChatSession;

/**
 * Created by jacob on 8/9/17.
 */

public class SessionStart implements ChatMessage {

    private String token;

    public String getToken() {
        return token;
    }

    public SessionStart setToken(String token) {
        this.token = token;
        return this;
    }

    @Override
    public void got(ChatSession chat) {
        chat.send(this);
    }
}
