package com.queatz.snappy.chat.actions;

import com.queatz.snappy.chat.ChatSession;
import com.queatz.snappy.shared.chat.SessionStartChatMessage;

/**
 * Created by jacob on 8/9/17.
 */

public class SessionStart extends SessionStartChatMessage implements ChatMessage {

    @Override
    public void got(ChatSession chat) {
        chat.setLocation(getLocation());
        chat.send((ChatMessage) new SessionStartResponse().setReplay(chat.getChat().getRecentEvents(getLocation())));
    }
}
