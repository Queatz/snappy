package com.queatz.snappy.adapter;

import android.app.Activity;
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
import com.queatz.snappy.team.Thing;
import com.queatz.snappy.util.Functions;
import com.squareup.picasso.Picasso;

import io.realm.DynamicRealmObject;
import io.realm.RealmBaseAdapter;
import io.realm.RealmResults;

/**
 * Created by jacob on 2/21/15.
 */
public class ContactAdapter extends RealmBaseAdapter<DynamicRealmObject> {
    public ContactAdapter(Activity context, RealmResults<DynamicRealmObject> realmResults) {
        super(context, realmResults);
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

        DynamicRealmObject contact = getItem(position);
        final DynamicRealmObject person = contact.getObject(Thing.TARGET);
        DynamicRealmObject message = contact.getObject(Thing.LATEST);

        boolean isOwn = message != null && team.auth.getUser() != null && team.auth.getUser().equals(message.getObject(Thing.SOURCE).getString(Thing.ID));

        TextView name = (TextView) view.findViewById(R.id.name);
        TextView lastMessage = (TextView) view.findViewById(R.id.lastMessage);
        ImageView profile = (ImageView) view.findViewById(R.id.profile);

        Picasso.with(context)
                .load(Functions.getImageUrlForSize(person, (int) Util.px(64)))
                .placeholder(R.color.spacer)
                .into(profile);

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                team.action.openProfile((Activity) context, person);
            }
        });

        name.setText(Functions.getFullName(person));
        name.setTypeface(null, !contact.getBoolean(Thing.SEEN) ? Typeface.BOLD : Typeface.NORMAL);

        if(message == null)
            lastMessage.setText("");
        else
            lastMessage.setText(isOwn ? String.format(context.getString(R.string.me_message), message.getString(Thing.MESSAGE)) : message.getString(Thing.MESSAGE));

        return view;
    }
}
