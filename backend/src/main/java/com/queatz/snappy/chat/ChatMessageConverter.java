package com.queatz.snappy.chat;

import com.queatz.snappy.chat.actions.AdAdd;
import com.queatz.snappy.chat.actions.ChatMessage;
import com.queatz.snappy.chat.actions.MessageSend;
import com.queatz.snappy.chat.actions.SessionStart;
import com.queatz.snappy.chat.actions.SessionStartResponse;
import com.queatz.snappy.logic.EarthJson;
import com.queatz.snappy.shared.chat.BasicChatMessage;
import com.queatz.snappy.shared.chat.ChatAction;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by jacob on 8/9/17.
 */

public class ChatMessageConverter {

    private static EarthJson json = new EarthJson();

    @Nullable
    public static ChatMessage convert(@NotNull BasicChatMessage basicChatMessage) {
        switch (basicChatMessage.getAction()) {
            case ChatAction.MESSAGE_SEND:
                return json.fromJson(basicChatMessage.getData(), MessageSend.class);
            case ChatAction.SESSION_START:
                return json.fromJson(basicChatMessage.getData(), SessionStart.class);
            case ChatAction.AD_ADD:
                return json.fromJson(basicChatMessage.getData(), AdAdd.class);
            default:
                return null;
        }
    }

    @NotNull
    public static BasicChatMessage convert(@NotNull ChatMessage chatMessage) {
        return new BasicChatMessage(toAction(chatMessage), json.toJsonTree(chatMessage));
    }

    @Nullable
    private static String toAction(ChatMessage chatMessage) {
        if (MessageSend.class.isAssignableFrom(chatMessage.getClass())) {
            return ChatAction.MESSAGE_SEND;
        }

        if (SessionStart.class.isAssignableFrom(chatMessage.getClass())) {
            return ChatAction.SESSION_START;
        }

        if (SessionStartResponse.class.isAssignableFrom(chatMessage.getClass())) {
            return ChatAction.SESSION_START;
        }

        if (AdAdd.class.isAssignableFrom(chatMessage.getClass())) {
            return ChatAction.AD_ADD;
        }

        return null;
    }
}
