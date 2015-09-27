package com.queatz.snappy.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.queatz.snappy.R;
import com.queatz.snappy.Util;
import com.queatz.snappy.things.Message;
import com.queatz.snappy.things.Person;
import com.squareup.picasso.Picasso;

import io.realm.RealmBaseAdapter;
import io.realm.RealmResults;

/**
 * Created by jacob on 2/21/15.
 */
public class PersonMessagesAdapter extends RealmBaseAdapter<Message> {
    Person mToPerson;

    public PersonMessagesAdapter(Context context, RealmResults<Message> realmResults, @NonNull Person to) {
        super(context, realmResults, true);
        mToPerson = to;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;

        if (convertView != null) {
            view = convertView;
        }
        else {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.person_messages_item, parent, false);
        }

        final Message message = realmResults.get(position);
        final Person person = message.getFrom();

        boolean isOwn = mToPerson.getId().equals(person.getId());

        RelativeLayout wrapper = ((RelativeLayout) view.findViewById(R.id.wrapper));
        wrapper.setGravity(isOwn ? Gravity.RIGHT : Gravity.LEFT);

        TextView textView = (TextView) view.findViewById(R.id.message);
        ImageView profile = (ImageView) view.findViewById(R.id.profile);



        if(isOwn)
            profile.bringToFront();
        else
            wrapper.bringToFront();

        Picasso.with(context)
                .load(person.getImageUrlForSize((int) Util.px(32)))
                .placeholder(R.color.spacer)
                .into(profile);

        textView.setText(message.getMessage());

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, Util.agoDate(message.getDate()), Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}
