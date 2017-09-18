package com.queatz.snappy.chat;

import android.location.Location;
import android.util.Log;

import com.koushikdutta.async.http.WebSocket;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.shared.Shared;
import com.queatz.snappy.shared.chat.BasicChatMessage;
import com.queatz.snappy.shared.chat.ChatAction;
import com.queatz.snappy.shared.chat.MessageSendChatMessage;
import com.queatz.snappy.shared.chat.SessionStartChatMessage;
import com.queatz.snappy.shared.chat.SessionStartResponseChatMessage;
import com.queatz.snappy.shared.earth.EarthGeo;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.util.Json;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import cz.msebera.android.httpclient.util.ByteArrayBuffer;

/**
 * Created by jacob on 9/17/17.
 */

public class ChatManager {

    private final Team team;

    private String chatToken;
    private List<ChatRoom> topics;
    private Map<String, List<MessageSendChatMessage>> messages;
    private List<String> defaultAvatars;
    private ChatRoom currentTopic;
    private String myAvatar;
    private WebSocket websocket;

    public ChatManager(Team team) {
        this.team = team;

        messages = new HashMap<>();
        topics = DefaultChatRooms.get();
        defaultAvatars = DefaultChatAvatars.get();
        currentTopic = topics.get(0);

        myAvatar = newRandomAvatar();

        chatToken = team.preferences.getString(Config.PREFERENCE_CHAT_TOKEN, null);

        if (chatToken == null) {
            chatToken = Shared.randomToken();
            team.preferences.edit().putString(Config.PREFERENCE_CHAT_TOKEN, chatToken).apply();
        }
    }

    public String newRandomAvatar() {
        return defaultAvatars.get(new Random().nextInt(defaultAvatars.size()));
    }

    public void got(String message) {
        got(Json.from(message, BasicChatMessage.class));
    }

    private void got(BasicChatMessage chat) {
        if (chat == null) {
            Log.w(Config.LOG_TAG, "chat - null");
            return;
        }

        if (chat.getAction() == null) {
            Log.w(Config.LOG_TAG, "chat - no action");
            return;
        }

        switch (chat.getAction()) {
            case ChatAction.MESSAGE_SEND:
                got(Json.from(chat.getData(), MessageSendChatMessage.class));
                break;
            case ChatAction.SESSION_START:
                got(Json.from(chat.getData(), SessionStartResponseChatMessage.class));
                break;
            default:
                Log.w(Config.LOG_TAG, "chat - unknown action: " + chat.getAction());
        }
    }

    public void got(MessageSendChatMessage chat) {
        if(!messages.containsKey(chat.getTopic())) {
            messages.put(chat.getTopic(), new ArrayList<MessageSendChatMessage>());
        }
        messages.get(chat.getTopic()).add(chat);

        if (!currentTopic.getName().equals(chat.getTopic())) {
            // XXX TODO Use ArangoDB for Android ... or realm
            for (ChatRoom topic : topics) {
                if (topic.getName().equals(chat.getTopic())) {
                    topic.setRecent(topic.getRecent() + 1);
                    break;
                }
            }
        }
    }

    public void got(SessionStartResponseChatMessage chat) {
        reset();

        for (BasicChatMessage basicChatMessage : chat.getReplay()) {
            got(basicChatMessage);
        }
    }

    private void reset() {
        for (Map.Entry<String, List<MessageSendChatMessage>> kv : messages.entrySet()) {
            kv.getValue().clear();
        }

        for (ChatRoom topic : topics) {
            topic.setRecent(0);

            if (topic.getAds() != null) {
                topic.getAds().clear();
            }
        }
    }

    public void start(Location location) {
        if (websocket == null) {
            return;
        }

        websocket.send(Json.to(new BasicChatMessage(ChatAction.SESSION_START, Json.tree(
                new SessionStartChatMessage()
                        .setLocation(EarthGeo.of(location.getLatitude(), location.getLongitude()))
                        .setToken(chatToken)
        ))));
    }

    public ChatManager setWebSocket(WebSocket websocket) {
        this.websocket = websocket;
        return this;
    }

    public List<ChatRoom> getTopics() {
        return topics;
    }

    public List<MessageSendChatMessage> getMessages(String topic) {
        if(!messages.containsKey(topic)) {
            messages.put(topic, new ArrayList<MessageSendChatMessage>());
        }

        return messages.get(topic);
    }

    public ChatRoom getCurrentTopic() {
        return currentTopic;
    }

    public String getMyAvatar() {
        return myAvatar;
    }

    public ChatManager setCurrentTopic(ChatRoom currentTopic) {
        this.currentTopic = currentTopic;
        currentTopic.setRecent(0);
        return this;
    }

    public ChatManager setMyAvatar(String myAvatar) {
        this.myAvatar = myAvatar;
        return this;
    }

    public void sendPhoto(String topic, byte[] file) {
        if (websocket == null) {
            return;
        }

        byte[] author = Json.to(new ChatAuthor()
                .setTopic(topic)
                .setAvatar(myAvatar)
        ).getBytes(Charset.forName("UTF-8"));

        ByteArrayBuffer bytes = new ByteArrayBuffer(author.length + 1 + file.length);

        bytes.append(author, 0, author.length);
        bytes.append(new byte[] { 0 }, 0, 1);
        bytes.append(file, 0, file.length);

        websocket.send(bytes.buffer());
    }

    public void close() {
        if (websocket != null) {
            websocket.close();
            websocket = null;
        }
    }

    public void send(MessageSendChatMessage messageSendChatMessage) {
        if (websocket == null) {
            return;
        }

        got(messageSendChatMessage);
        websocket.send(Json.to(new BasicChatMessage(ChatAction.MESSAGE_SEND, Json.tree(messageSendChatMessage))));
    }
}
