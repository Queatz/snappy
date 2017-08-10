package com.queatz.snappy.chat.actions;

import com.queatz.snappy.chat.BasicChatMessage;
import com.queatz.snappy.chat.ChatSession;

import java.util.List;

/**
 * Created by jacob on 8/9/17.
 */

public class SessionStartResponse implements ChatMessage {

    private List<BasicChatMessage> replay;

    public SessionStartResponse() {}

    public SessionStartResponse(List<BasicChatMessage> replay) {
        this.replay = replay;
    }

    public List<BasicChatMessage> getReplay() {
        return replay;
    }

    public SessionStartResponse setReplay(List<BasicChatMessage> replay) {
        this.replay = replay;
        return this;
    }

    @Override
    public void got(ChatSession chat) {

    }
}
