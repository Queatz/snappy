package com.queatz.snappy.shared.chat;

import java.util.List;

/**
 * Created by jacob on 9/17/17.
 */

public class SessionStartResponseChatMessage {

    private List<BasicChatMessage> replay;

    public List<BasicChatMessage> getReplay() {
        return replay;
    }

    public SessionStartResponseChatMessage setReplay(List<BasicChatMessage> replay) {
        this.replay = replay;
        return this;
    }

}
