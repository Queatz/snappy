package com.queatz.snappy.chat;

import com.queatz.snappy.chat.actions.ChatMessage;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by jacob on 8/9/17.
 */

public class ChatLogic extends ChatEndpoint {

    private final Set<ChatSession> sessions = new HashSet<>();

    public void broadcast(ChatSession chat, ChatMessage message) {
        synchronized (sessions) {
            for (ChatSession session : sessions) {
                if (!session.getSession().isOpen()) {
                    continue;
                }

                if (chat.getSession().getId().equals(session.getSession().getId())) {
                    continue;
                }

                session.send(message);
            }
        }
    }

    public void join(ChatSession chat) {
        synchronized (sessions) {
            sessions.add(chat);
        }
    }

    public void leave(ChatSession chat) {
        synchronized (sessions) {
            sessions.remove(chat);
        }
    }
}
