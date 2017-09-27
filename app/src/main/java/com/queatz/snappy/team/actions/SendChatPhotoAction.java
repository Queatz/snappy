package com.queatz.snappy.team.actions;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.preference.PreferenceManager;

import com.queatz.snappy.chat.ChatManager;
import com.queatz.snappy.chat.ChatRoom;
import com.queatz.snappy.shared.Config;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by jacob on 9/18/17.
 *
 * @emit true on completion
 */

public class SendChatPhotoAction extends ActivityAction {

    private final ChatManager chatManager;
    private final ChatRoom topic;

    public SendChatPhotoAction(ChatManager chatManager) {
        this.chatManager = chatManager;
        this.topic = chatManager.getCurrentTopic();
    }

    @Override
    protected void execute() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        me().getActivity().startActivityForResult(intent, Config.REQUEST_CODE_SEND_CHAT_PHOTO);
        getTeam().callbacks.set(Config.REQUEST_CODE_SEND_CHAT_PHOTO, new PreferenceManager.OnActivityResultListener() {
            @Override
            public boolean onActivityResult(int requestCode, int resultCode, Intent intent) {
                emit(resultCode == Activity.RESULT_OK);
                if(resultCode == Activity.RESULT_OK) {
                    final Uri photo = intent.getData();

                    if (photo == null) {
                        return false;
                    }

                    try {
                        InputStream inputStream = getTeam().context.getContentResolver().openInputStream(photo);

                        if (inputStream == null) {
                            return false;
                        }

                        byte[] bytes = new byte[inputStream.available()];
                        inputStream.read(bytes, 0, inputStream.available());

                        chatManager.sendPhoto(topic.getName(), bytes);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return false;
                    }
                }

                return false;
            }
        });
    }
}
