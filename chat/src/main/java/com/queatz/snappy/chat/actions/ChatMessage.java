package com.queatz.snappy.chat.actions;

import com.queatz.snappy.chat.ChatSession;

/**
 * Created by jacob on 8/9/17.
 */

public interface ChatMessage {
    void got(ChatSession chat);
}
