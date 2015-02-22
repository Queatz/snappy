package com.queatz.snappy.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.queatz.snappy.MainApplication;
import com.queatz.snappy.R;
import com.queatz.snappy.Util;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.things.Contact;
import com.queatz.snappy.things.Message;
import com.queatz.snappy.things.Person;
import com.squareup.picasso.Picasso;

import io.realm.RealmBaseAdapter;
import io.realm.RealmResults;

/**
 * Created by jacob on 2/21/15.
 */
public class ContactAdapter extends RealmBaseAdapter<Contact> {
    public ContactAdapter(Context context, RealmResults<Contact> realmResults) {
        super(context, realmResults, true);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;

        final Team team = ((MainApplication) context.getApplicationContext()).team;

        if (convertView != null) {
            view = convertView;
        }
        else {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.messages_item, parent, false);
        }

        Contact contact = realmResults.get(position);
        Person person = contact.getContact();
        Message message = contact.getLast();

        boolean isOwn = message != null && team.auth.getUser().equals(message.getFrom().getId());

        TextView name = (TextView) view.findViewById(R.id.name);
        TextView lastMessage = (TextView) view.findViewById(R.id.lastMessage);
        ImageView profile = (ImageView) view.findViewById(R.id.profile);

        Picasso.with(context)
                .load(person.getImageUrlForSize((int) Util.px(64)))
                .placeholder(R.color.spacer)
                .into(profile);

        name.setText(person.getName());
        name.setTypeface(name.getTypeface(), contact.isUnread() ? Typeface.BOLD : Typeface.NORMAL);

        if(message == null)
            lastMessage.setText("");
        else
            lastMessage.setText(isOwn ? String.format(context.getString(R.string.me_message), message.getMessage()) : message.getMessage());

        return view;
    }
}
