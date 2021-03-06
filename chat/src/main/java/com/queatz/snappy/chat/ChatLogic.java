package com.queatz.snappy.chat;

import com.queatz.chat.ChatConfig;
import com.queatz.chat.ChatKind;
import com.queatz.chat.ChatWorld;
import com.queatz.earth.EarthField;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.chat.actions.AdAdd;
import com.queatz.snappy.chat.actions.ChatMessage;
import com.queatz.snappy.chat.actions.MessageSend;
import com.queatz.snappy.shared.Shared;
import com.queatz.snappy.shared.chat.BasicChatMessage;
import com.queatz.snappy.shared.earth.EarthGeo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Created by jacob on 8/9/17.
 */

public class ChatLogic extends ChatEndpoint {

    private final Set<ChatSession> sessions = new HashSet<>();
    private final ChatWorld chatWorld = new ChatWorld();

    public void broadcast(ChatSession chat, ChatMessage message) {
        synchronized (sessions) {
            for (ChatSession other : sessions) {
                if (!other.getSession().isOpen()) {
                    continue;
                }

                if (chat.getSession().getId().equals(other.getSession().getId())) {
                    continue;
                }

                if (Shared.distance(chat.getLocation(), other.getLocation()) > ChatConfig.MAX_RADIUS) {
                    Logger.getAnonymousLogger().warning("Distance too far");
                    continue;
                }

                other.send(message);
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

    public List<BasicChatMessage> getRecentEvents(EarthGeo location) {
        List<EarthThing> things = chatWorld.near(location);

        List<BasicChatMessage> result = new ArrayList<>();

        for (EarthThing thing : things) {
            switch (thing.getString(EarthField.KIND)) {
                case ChatKind.AD_KIND:
                    result.add(ChatMessageConverter.convert((ChatMessage) new AdAdd()
                            .setSource(thing.getString(EarthField.SOURCE))
                            .setDate(thing.getDate(EarthField.CREATED_ON))
                            .setDescription(thing.getString(EarthField.ABOUT))
                            .setName(thing.getString(EarthField.NAME))
                            .setTopic(thing.getString(EarthField.TOPIC))));
                    break;
                case ChatKind.MESSAGE_KIND:
                    result.add(ChatMessageConverter.convert((ChatMessage) new MessageSend()
                            .setTopic(thing.getString(EarthField.TOPIC))
                            .setPhoto(thing.getString(EarthField.PHOTO))
                            .setDate(thing.getDate(EarthField.CREATED_ON))
                            .setAvatar(thing.getString(EarthField.IMAGE_URL))
                            .setMessage(thing.getString(EarthField.MESSAGE))));
                    break;
                default:
                    Logger.getAnonymousLogger().info("Skipping chat world thing: " + thing.getString(EarthField.KIND));
            }
        }

        return result;
    }

    public ChatWorld getWorld() {
        return chatWorld;
    }
}
