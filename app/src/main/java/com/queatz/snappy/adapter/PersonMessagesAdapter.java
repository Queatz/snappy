package com.queatz.snappy.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.queatz.snappy.R;
import com.queatz.snappy.Util;
import com.queatz.snappy.team.Thing;
import com.queatz.snappy.util.Functions;
import com.queatz.snappy.util.TimeUtil;
import com.squareup.picasso.Picasso;

import io.realm.DynamicRealmObject;
import io.realm.RealmBaseAdapter;
import io.realm.RealmResults;

/**
 * Created by jacob on 2/21/15.
 */
public class PersonMessagesAdapter extends RealmBaseAdapter<DynamicRealmObject> {
    DynamicRealmObject mToPerson;

    public PersonMessagesAdapter(Context context, RealmResults<DynamicRealmObject> realmResults, @NonNull DynamicRealmObject to) {
        super(context, realmResults);
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

        final DynamicRealmObject message = getItem(position);
        final DynamicRealmObject person = message.getObject(Thing.FROM);

        boolean isOwn = mToPerson.getString(Thing.ID).equals(person.getString(Thing.ID));

        RelativeLayout wrapper = ((RelativeLayout) view.findViewById(R.id.wrapper));
        wrapper.setGravity(isOwn ? Gravity.RIGHT : Gravity.LEFT);

        TextView textView = (TextView) view.findViewById(R.id.message);
        ImageView profile = (ImageView) view.findViewById(R.id.profile);

        if(isOwn)
            profile.bringToFront();
        else
            wrapper.bringToFront();

        Picasso.with(context)
                .load(Functions.getImageUrlForSize(person, (int) Util.px(32)))
                .placeholder(R.color.spacer)
                .into(profile);

        textView.setText(message.getString(Thing.MESSAGE));

        textView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    Toast.makeText(context, TimeUtil.agoDate(message.getDate(Thing.DATE)), Toast.LENGTH_SHORT).show();
                }

                return false;
            }
        });

        return view;
    }
}
