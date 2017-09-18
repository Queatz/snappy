package com.queatz.snappy.chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.queatz.snappy.R;
import com.queatz.snappy.ui.PixelatedTransform;
import com.queatz.snappy.util.Images;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jacob on 9/18/17.
 */

public class ChatTopicAdapter extends BaseAdapter {

    private List<ChatRoom> topics;
    private Context context;

    public ChatTopicAdapter(Context context) {
        this.context = context;
        topics = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return topics.size();
    }

    @Override
    public ChatRoom getItem(int position) {
        return topics.get(position);
    }

    @Override
    public long getItemId(int position) {
        return topics.get(position).hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;

        if (convertView != null) {
            view = convertView;
        } else {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.chat_topic, parent, false);
        }

        ChatRoom topic = getItem(position);

        final ImageView img = (ImageView) view.findViewById(R.id.img);
        final TextView name = (TextView) view.findViewById(R.id.name);
        final TextView recent = (TextView) view.findViewById(R.id.recent);

        if (topic == null) {
            name.setText("");
            return view;
        }

        name.setText(topic.getName());

        Images.with(context).cancelRequest(img);

        Images.with(context)
                .load(ChatUtil.defaultTopicImg(topic.getName()))
                .placeholder(R.color.spacer)
                .transform(new PixelatedTransform())
                .into(img);

        if (topic.getRecent() > 0) {
            recent.setVisibility(View.VISIBLE);
            recent.setText(context.getString(R.string.chat_topic_recent, String.valueOf(topic.getRecent())));
        } else {
            recent.setVisibility(View.GONE);
        }

        return view;
    }

    public ChatTopicAdapter setTopics(List<ChatRoom> messages) {
        this.topics = messages == null ? new ArrayList<ChatRoom>() : messages;
        notifyDataSetChanged();
        return this;
    }
}
