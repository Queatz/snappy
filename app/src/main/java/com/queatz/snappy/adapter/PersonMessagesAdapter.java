package com.queatz.snappy.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.queatz.branch.Branch;
import com.queatz.snappy.MainApplication;
import com.queatz.snappy.R;
import com.queatz.snappy.Util;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.team.Thing;
import com.queatz.snappy.team.contexts.ActivityContext;
import com.queatz.snappy.util.Functions;
import com.queatz.snappy.util.Images;
import com.queatz.snappy.util.LocalState;
import com.queatz.snappy.util.TimeUtil;

import io.realm.DynamicRealmObject;
import io.realm.RealmBaseAdapter;
import io.realm.RealmResults;

/**
 * Created by jacob on 2/21/15.
 */
public class PersonMessagesAdapter extends RealmBaseAdapter<DynamicRealmObject> {
    private final Context context;
    private DynamicRealmObject mToPerson;

    public PersonMessagesAdapter(Context context, RealmResults<DynamicRealmObject> realmResults, @NonNull DynamicRealmObject to) {
        super(realmResults);
        this.context = context;
        mToPerson = to;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Branch<ActivityContext> branch = Branch.from((ActivityContext) context);

        View view;

        if (convertView != null) {
            view = convertView;
        } else {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.person_messages_item, parent, false);
        }

        final DynamicRealmObject message = getItem(position);
        final DynamicRealmObject person = message.getObject(Thing.FROM);

        final boolean isOwn = mToPerson.getString(Thing.ID).equals(person.getString(Thing.ID));

        LinearLayout wrapper = ((LinearLayout) view.findViewById(R.id.wrapper));
        wrapper.setGravity(isOwn ? Gravity.RIGHT : Gravity.LEFT);

        TextView textView = (TextView) view.findViewById(R.id.message);
        ImageView photo = (ImageView) view.findViewById(R.id.photo);
        ImageView profile = (ImageView) view.findViewById(R.id.profile);

        if(isOwn) {
            profile.bringToFront();
        } else {
            wrapper.bringToFront();
        }

        Images.with(context)
                .load(Functions.getImageUrlForSize(person, (int) Util.px(32)))
                .placeholder(R.color.spacer)
                .into(profile);

        View.OnTouchListener timeTap = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    Toast.makeText(context, TimeUtil.agoDate(message.getDate(Thing.DATE)), Toast.LENGTH_SHORT).show();
                }

                return false;
            }
        };

        View.OnTouchListener resendTap = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Team team = ((MainApplication) context.getApplicationContext()).team;

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    DynamicRealmObject who = isOwn ? message.getObject(Thing.TO) : person;

                    branch.to(new OpenMessagesAction(who, message.getString(Thing.MESSAGE)));
                }

                return false;
            }
        };

        if (!message.isNull(Thing.PHOTO) && message.getBoolean(Thing.PHOTO)) {
            photo.setVisibility(View.VISIBLE);
            Images.with(context)
                    .load(Util.locationPhoto(message, parent.getMeasuredWidth()))
                    .placeholder(R.color.spacer)
                    .into(photo);

            photo.setClickable(true);
            photo.setOnTouchListener(timeTap);
        } else {
            photo.setVisibility(View.GONE);
        }

        textView.setText(message.getString(Thing.MESSAGE));

        if (LocalState.UNSYNCED.equals(message.getString(Thing.LOCAL_STATE))) {
            textView.setTextColor(context.getResources().getColor(R.color.gray));
            textView.setOnTouchListener(resendTap);
        } else {
            textView.setTextColor(context.getResources().getColor(R.color.black));
            textView.setOnTouchListener(timeTap);
        }

        return view;
    }
}
