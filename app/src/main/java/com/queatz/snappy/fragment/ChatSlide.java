package com.queatz.snappy.fragment;

import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.WebSocket;
import com.queatz.snappy.R;
import com.queatz.snappy.chat.ChatManager;
import com.queatz.snappy.chat.ChatMessageAdapter;
import com.queatz.snappy.chat.ChatUtil;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.shared.chat.MessageSendChatMessage;
import com.queatz.snappy.team.TeamFragment;
import com.queatz.snappy.ui.EditText;
import com.queatz.snappy.ui.PixelatedTransform;
import com.queatz.snappy.util.Images;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import cz.msebera.android.httpclient.conn.ssl.SSLConnectionSocketFactory;

/**
 * Created by jacob on 9/17/17.
 */

public class ChatSlide extends TeamFragment {

    private ChatManager chatManager;
    private ChatMessageAdapter chatMessageAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        chatManager = new ChatManager(getTeam());

        connect();
    }

    private void connect() {
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

        AsyncHttpClient.getDefaultInstance()
                .websocket(Config.WS_URI, "RFC6570", new AsyncHttpClient.WebSocketConnectCallback() {
                    @Override
                    public void onCompleted(final Exception ex, WebSocket webSocket) {
                        if (ex != null) {
                            ex.printStackTrace();
                            return;
                        }

                        chatManager.setWebSocket(webSocket);

                        Location location = getTeam().location.get();

                        webSocket.setStringCallback(new WebSocket.StringCallback() {
                            public void onStringAvailable(final String message) {
                                chatManager.got(message);
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        refreshViews();
                                    }
                                });
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

                        if (location != null) {
                            chatManager.start(location);
                        } else {
                            getTeam().location.get(getActivity(), new com.queatz.snappy.team.Location.OnLocationFoundCallback() {
                                @Override
                                public void onLocationFound(Location location) {
                                    chatManager.start(location);
                                }

                                @Override
                                public void onLocationUnavailable() {

                                }
                            });
                        }
                    }
                });
    }

    private void refreshViews() {
        chatMessageAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        chatManager.close();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.chat, container, false);

        ListView chatList = (ListView) view.findViewById(R.id.chatList);

        chatMessageAdapter = new ChatMessageAdapter(getActivity());
        chatMessageAdapter.setMessages(chatManager.getMessages(chatManager.getCurrentTopic().getName()));
        chatList.setAdapter(chatMessageAdapter);

        final ImageButton avatarButton = (ImageButton) view.findViewById(R.id.avatarButton);
        final EditText chatHere = (EditText) view.findViewById(R.id.chatHere);
        final ImageView sendButton = (ImageView) view.findViewById(R.id.sendButton);

        avatarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chatManager.setMyAvatar(chatManager.newRandomAvatar());
                showMyAvatar(view);
            }
        });

        showChatHint(view);
        showMyAvatar(view);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = chatHere.getText().toString().trim();

                if (message.isEmpty()) {
                    return;
                }

                chatManager.send(new MessageSendChatMessage()
                        .setAvatar(chatManager.getMyAvatar())
                        .setTopic(chatManager.getCurrentTopic().getName())
                        .setMessage(message)
                );
                refreshViews();

                chatHere.setText("");
            }
        });

        chatHere.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                switch (actionId) {
                    case EditorInfo.IME_ACTION_SEND:
                        sendButton.callOnClick();
                }

                return true;
            }
        });

        return view;
    }

    private void showChatHint(View view) {
        EditText chatHere = (EditText) view.findViewById(R.id.chatHere);

        chatHere.setHint(getResources().getString(R.string.chat_in, chatManager.getCurrentTopic().getName()));
    }

    private void showMyAvatar(View view) {
        ImageButton avatarButton = (ImageButton) view.findViewById(R.id.avatarButton);

        Images.with(getActivity()).cancelRequest(avatarButton);
        Images.with(getActivity())
                .load(ChatUtil.defaultAvatarImg(chatManager.getMyAvatar()))
                .transform(new PixelatedTransform())
                .into(avatarButton);
    }
}
