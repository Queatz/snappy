package com.queatz.snappy.shared.chat;

import com.queatz.snappy.shared.earth.EarthGeo;

/**
 * Created by jacob on 9/17/17.
 */

public class SessionStartChatMessage {
    private String token;
    private EarthGeo location;

    public String getToken() {
        return token;
    }

    public SessionStartChatMessage setToken(String token) {
        this.token = token;
        return this;
    }

    public EarthGeo getLocation() {
        return location;
    }

    public SessionStartChatMessage setLocation(EarthGeo location) {
        this.location = location;
        return this;
    }
}
