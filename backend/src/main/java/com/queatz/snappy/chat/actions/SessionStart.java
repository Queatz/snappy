package com.queatz.snappy.chat.actions;

import com.queatz.snappy.chat.ChatSession;
import com.queatz.snappy.logic.EarthGeo;

/**
 * Created by jacob on 8/9/17.
 */

public class SessionStart implements ChatMessage {

    private String token;
    private EarthGeo location;

    public String getToken() {
        return token;
    }

    public SessionStart setToken(String token) {
        this.token = token;
        return this;
    }

    public EarthGeo getLocation() {
        return location;
    }

    public SessionStart setLocation(EarthGeo location) {
        this.location = location;
        return this;
    }

    @Override
    public void got(ChatSession chat) {
        chat.setLocation(location);
        chat.send(new SessionStartResponse(chat.getChat().getRecentEvents(location)));
    }
}
