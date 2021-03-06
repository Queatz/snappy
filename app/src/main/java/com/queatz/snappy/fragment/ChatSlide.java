package com.queatz.snappy.fragment;

import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.queatz.branch.Branch;
import com.queatz.snappy.R;
import com.queatz.snappy.chat.ChatManager;
import com.queatz.snappy.chat.ChatMessageAdapter;
import com.queatz.snappy.chat.ChatRoom;
import com.queatz.snappy.chat.ChatTopicAdapter;
import com.queatz.snappy.chat.ChatUtil;
import com.queatz.snappy.chat.Locality;
import com.queatz.snappy.chat.OnChatChangedCallback;
import com.queatz.snappy.shared.chat.MessageSendChatMessage;
import com.queatz.snappy.team.TeamFragment;
import com.queatz.snappy.team.actions.SendChatPhotoAction;
import com.queatz.snappy.ui.EditText;
import com.queatz.snappy.ui.FadePulseAnimation;
import com.queatz.snappy.ui.PixelatedTransform;
import com.queatz.snappy.ui.RevealAnimation;
import com.queatz.snappy.util.Images;

/**
 * Created by jacob on 9/17/17.
 */

public class ChatSlide extends TeamFragment {

    private ChatManager chatManager;
    private ChatMessageAdapter chatMessageAdapter;
    private ChatTopicAdapter chatTopicAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        chatManager = new ChatManager(getActivity(), getTeam(), new OnChatChangedCallback() {
            @Override
            public void onContentChanged() {
                if (getActivity() == null) {
                    return;
                }

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        refreshViews();
                    }
                });
            }

            @Override
            public void onLocationChanged(Location location) {
                if (getTeam().locality.get() != null) {
                    setLocality(getTeam().locality.get());
                }

                findLocality(location);
            }

            @Override
            public void onPhotoUploaded() {
                if (getView() == null || getActivity() == null) {
                    return;
                }

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        final ImageView sendButton = getView().findViewById(R.id.sendButton);
                        sendButton.clearAnimation();
                        sendButton.setAlpha(1f);
                    }
                });
            }

            @Override
            public void onConnectionChange(final boolean isConnected) {
                if (getView() == null || getActivity() == null) {
                    return;
                }

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        final View reconnecting = getView().findViewById(R.id.reconnecting);
                        if (isConnected) {
                            if (reconnecting.getVisibility() != View.GONE) {
                                RevealAnimation.collapse(reconnecting);
                            }
                        } else {
                            if (reconnecting.getVisibility() == View.GONE) {
                                RevealAnimation.expand(reconnecting);
                            }
                        }
                    }
                });
            }
        });

        chatManager.connect();
    }

    private void refreshViews() {
        chatMessageAdapter.notifyDataSetChanged();
        chatTopicAdapter.notifyDataSetChanged();

        if (getView() != null) {
            showChatHint(getView());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        chatManager.close();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.chat, container, false);

        ListView chatList = view.findViewById(R.id.chatList);
        ListView topicsList = view.findViewById(R.id.topicsList);

        chatTopicAdapter = new ChatTopicAdapter(getActivity());
        chatTopicAdapter.setTopics(chatManager.getTopics());
        topicsList.setAdapter(chatTopicAdapter);
        topicsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ChatRoom next = chatTopicAdapter.getItem(position);

                if (next == null) {
                    return;
                }

                setTopic(next);
            }
        });

        chatMessageAdapter = new ChatMessageAdapter(getActivity());
        showMessagesForTopic();
        chatList.setAdapter(chatMessageAdapter);

        final ImageView avatarButton = view.findViewById(R.id.avatarButton);
        final EditText chatHere = view.findViewById(R.id.chatHere);
        final ImageView sendButton = view.findViewById(R.id.sendButton);

        avatarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chatManager.setMyAvatar(chatManager.newRandomAvatar());
                showMyAvatar(view);
            }
        });

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (hasTypedAnything()) {
                    sendButton.setImageResource(R.drawable.ic_send_white_24dp);
                } else {
                    sendButton.setImageResource(R.drawable.ic_add_a_photo_white_24dp);
                }
            }
        };

        chatHere.addTextChangedListener(textWatcher);

        showChatHint(view);
        showMyAvatar(view);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hasTypedAnything()) {
                    sendMessage();
                } else {
                    sendPhoto();
                }
            }
        });

        chatHere.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                switch (actionId) {
                    case EditorInfo.IME_ACTION_SEND:
                        sendMessage();
                        break;
                }

                return true;
            }
        });

        return view;
    }

    public void setTopic(String topic) {
        setTopic(chatManager.getTopic(topic));
    }

    private void setTopic(ChatRoom chatRoom) {
        if (chatRoom == null) {
            return;
        }

        chatManager.setCurrentTopic(chatRoom);
        showMessagesForTopic();
        refreshViews();
    }

    private void sendPhoto() {
        if (getView() == null) {
            return;
        }

        final ImageView sendButton = getView().findViewById(R.id.sendButton);

        to(new SendChatPhotoAction(chatManager).when(Boolean.class, new Branch<Boolean>() {
            @Override
            protected void execute() {
                if (me()) {
                    final Animation animation = new FadePulseAnimation(sendButton);
                    sendButton.startAnimation(animation);
                }
            }
        }));
    }

    private void sendMessage() {
        if (getView() == null) {
            return;
        }

        EditText chatHere = getView().findViewById(R.id.chatHere);

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

    private boolean hasTypedAnything() {
        if (getView() == null) {
            return false;
        }

        return !((EditText) getView().findViewById(R.id.chatHere)).getText().toString().trim().isEmpty();
    }

    private void showMessagesForTopic() {
        chatMessageAdapter.setMessages(chatManager.getMessages(chatManager.getCurrentTopic().getName()));
    }

    private void showChatHint(View view) {
        EditText chatHere = (EditText) view.findViewById(R.id.chatHere);

        chatHere.setHint(getResources().getString(R.string.chat_in, chatManager.getCurrentTopic().getName()));
    }

    private void showMyAvatar(View view) {
        ImageView avatarButton = (ImageView) view.findViewById(R.id.avatarButton);

        Images.with(getActivity()).cancelRequest(avatarButton);
        Images.with(getActivity())
                .load(ChatUtil.defaultAvatarImg(chatManager.getMyAvatar()))
                .transform(new PixelatedTransform())
                .into(avatarButton);
    }

    private void findLocality(Location location) {
        getTeam().locality.get(location, new Locality.OnLocalityFound() {
            @Override
            public void onLocalityFound(String locality) {
                setLocality(locality);
            }
        });
    }

    private void setLocality(final String locality) {
        if (getView() == null || getActivity() == null) {
            return;
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView chatLocality = getView().findViewById(R.id.chatLocality);
                chatLocality.setText(getString(R.string.locality_chats, locality.toUpperCase()));
            }
        });
    }
}
