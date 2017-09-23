package com.queatz.snappy.chat;

import android.app.Activity;
import android.location.Location;
import android.util.Log;

import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.http.AsyncHttpClient;
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
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import cz.msebera.android.httpclient.conn.ssl.SSLConnectionSocketFactory;
import cz.msebera.android.httpclient.util.ByteArrayBuffer;

/**
 * Created by jacob on 9/17/17.
 */

public class ChatManager {

    private final Team team;
    private final Activity activity;
    private final OnChatChangedCallback onChatChangedCallback;

    private String chatToken;
    private boolean isConnecting;
    private List<ChatRoom> topics;
    private Map<String, List<MessageSendChatMessage>> messages;
    private List<String> defaultAvatars;
    private ChatRoom currentTopic;
    private String myAvatar;
    private WebSocket websocket;
    private final Queue<Object> queue = new ConcurrentLinkedQueue<>();

    public ChatManager(Activity activity, Team team, OnChatChangedCallback onChatChangedCallback) {
        this.team = team;
        this.activity = activity;
        this.onChatChangedCallback = onChatChangedCallback;

        messages = new HashMap<>();
        topics = DefaultChatRooms.get();
        defaultAvatars = DefaultChatAvatars.get();
        currentTopic = topics.get(0);

        myAvatar = team.preferences.getString(Config.PREFERENCE_CHAT_AVATAR, null);

        if (myAvatar == null) {
            setMyAvatar(newRandomAvatar());
        }

        chatToken = team.preferences.getString(Config.PREFERENCE_CHAT_TOKEN, null);

        if (chatToken == null) {
            chatToken = Shared.randomToken();
            team.preferences.edit().putString(Config.PREFERENCE_CHAT_TOKEN, chatToken).apply();
        }
    }

    public void connect() {
        SSLContext sslContext;

        X509TrustManager tm = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };

        try {
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[] { tm }, null);
        } catch (Exception e1) {
            e1.printStackTrace();
            return;
        }

        AsyncHttpClient.getDefaultInstance().getSSLSocketMiddleware().setSSLContext(sslContext);
        AsyncHttpClient.getDefaultInstance().getSSLSocketMiddleware().setHostnameVerifier(SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        AsyncHttpClient.getDefaultInstance().getSSLSocketMiddleware().setTrustManagers(new TrustManager[] { tm });

        isConnecting = true;
        AsyncHttpClient.getDefaultInstance()
                .websocket(Config.WS_URI, "RFC6570", new AsyncHttpClient.WebSocketConnectCallback() {
                    @Override
                    public void onCompleted(final Exception ex, WebSocket webSocket) {
                        isConnecting = false;

                        if (ex != null) {
                            ex.printStackTrace();
                            return;
                        }

                        setWebSocket(webSocket);

                        webSocket.setStringCallback(new WebSocket.StringCallback() {
                            public void onStringAvailable(final String message) {
                                got(message);
                                if (onChatChangedCallback != null) {
                                    onChatChangedCallback.onContentChanged();
                                }
                            }
                        });

                        webSocket.setClosedCallback(new CompletedCallback() {
                            @Override
                            public void onCompleted(Exception e) {
                                if (e != null) {
                                    e.printStackTrace();
                                }

                                connect();
                            }
                        });

                        webSocket.setEndCallback(new CompletedCallback() {
                            @Override
                            public void onCompleted(Exception e) {
                                if (e != null) {
                                    e.printStackTrace();
                                }

                                connect();
                            }
                        });

                        Location location = team.location.get();

                        if (location != null) {
                            start(location);

                            if (onChatChangedCallback != null) {
                                onChatChangedCallback.onLocationChanged(location);
                            }
                        } else {
                            team.location.get(activity, new com.queatz.snappy.team.Location.OnLocationFoundCallback() {
                                @Override
                                public void onLocationFound(Location location) {
                                    start(location);

                                    if (onChatChangedCallback != null) {
                                        onChatChangedCallback.onLocationChanged(location);
                                    }
                                }

                                @Override
                                public void onLocationUnavailable() {

                                }
                            });
                        }
                    }
                });
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
        if (!connected()) {
            queue(location);
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

        while (!queue.isEmpty()) {
            Object item = queue.remove();

            if (item instanceof MessageSendChatMessage) {
                send((MessageSendChatMessage) item);
            } else if (item instanceof SendPhotoToTopic) {
                sendPhoto(
                        ((SendPhotoToTopic) item).getTopic(),
                        ((SendPhotoToTopic) item).getFile()
                );
            } else if (item instanceof Location) {
                start((Location) item);
            } else {
                Log.w(Config.LOG_TAG, "chat - unknown queue item: " + item);

            }
        }

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
        team.preferences.edit().putString(Config.PREFERENCE_CHAT_AVATAR, myAvatar).apply();
        return this;
    }

    public void sendPhoto(String topic, byte[] file) {
        if (!connected()) {
            queue(new SendPhotoToTopic(topic, file));
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

    public boolean connected() {
        return websocket != null && websocket.isOpen();
    }

    public void close() {
        if (websocket != null) {
            websocket.close();
            websocket = null;
        }
    }

    public void send(MessageSendChatMessage messageSendChatMessage) {
        if (!connected()) {
            queue(messageSendChatMessage);
            return;
        }

        got(messageSendChatMessage);
        websocket.send(Json.to(new BasicChatMessage(ChatAction.MESSAGE_SEND, Json.tree(messageSendChatMessage))));
    }

    private void queue(Object o) {
        queue.add(o);

        if (!connected() && !isConnecting) {
            connect();
        }
    }
}
