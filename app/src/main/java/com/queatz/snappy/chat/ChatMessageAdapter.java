package com.queatz.snappy.chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.queatz.snappy.R;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.shared.chat.MessageSendChatMessage;
import com.queatz.snappy.ui.PixelatedTransform;
import com.queatz.snappy.util.Images;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jacob on 9/17/17.
 */

public class ChatMessageAdapter extends BaseAdapter {

    private List<MessageSendChatMessage> messages;
    private Context context;

    public ChatMessageAdapter(Context context) {
        this.context = context;
        messages = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public MessageSendChatMessage getItem(int position) {
        return messages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return messages.get(position).hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;

        if (convertView != null) {
            view = convertView;
        } else {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.chat_message, parent, false);
        }

        MessageSendChatMessage chat = getItem(position);

        TextView message = (TextView) view.findViewById(R.id.message);
        ImageView avatar = (ImageView) view.findViewById(R.id.avatar);
        ImageView photo = (ImageView) view.findViewById(R.id.photo);

        if (chat == null) {
            message.setText("");
            return view;
        }

        if (chat.getPhoto() != null) {
            photo.setVisibility(View.VISIBLE);
            message.setVisibility(View.GONE);

            Images.with(context).cancelRequest(photo);

            Images.with(context)
                    .load(Config.BASE_URL + chat.getPhoto())
                    .placeholder(R.color.spacer)
                    .into(photo);
        } else {
            photo.setVisibility(View.GONE);
            message.setVisibility(View.VISIBLE);
            message.setText(chat.getMessage());
        }

        avatar.setImageDrawable(null);

        Images.with(context).cancelRequest(avatar);

        if (chat.getAvatar() != null) {
            Images.with(context)
                    .load(ChatUtil.defaultAvatarImg(chat.getAvatar()))
                    .placeholder(R.color.spacer)
                    .transform(new PixelatedTransform())
                    .into(avatar);
        }

        return view;
    }

    public ChatMessageAdapter setMessages(List<MessageSendChatMessage> messages) {
        this.messages = messages == null ? new ArrayList<MessageSendChatMessage>() : messages;
        notifyDataSetChanged();
        return this;
    }
}
