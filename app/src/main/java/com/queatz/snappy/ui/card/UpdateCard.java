package com.queatz.snappy.ui.card;

import android.app.Activity;
import android.content.Context;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.queatz.snappy.MainApplication;
import com.queatz.snappy.R;
import com.queatz.snappy.Util;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.team.Thing;
import com.queatz.snappy.util.Functions;
import com.queatz.snappy.util.TimeUtil;
import com.squareup.picasso.Picasso;

import io.realm.DynamicRealmObject;

/**
 * Created by jacob on 8/22/16.
 */
public class UpdateCard implements Card<DynamicRealmObject> {
    @Override
    public View getCard(final Context context, final DynamicRealmObject update, View convertView, ViewGroup parent) {
        return getCard(context, update, convertView, parent, false);
    }

    public View getCard(final Context context, final DynamicRealmObject update, View convertView, ViewGroup parent, boolean isFloating) {
        final View view;

        if (convertView != null) {
            view = convertView;
        }
        else {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.person_upto_item, parent, false);
        }

        if (isFloating) {
            view.setBackgroundResource(R.drawable.white_rounded);
            view.findViewById(R.id.highlight).setBackgroundResource(R.drawable.blue_rounded_top);
            view.setElevation(0);
        } else {
            view.setBackgroundResource(R.color.white);
            view.findViewById(R.id.highlight).setBackgroundResource(R.color.blue);
            view.setElevation(Util.px(2));
        }

        final Team team = ((MainApplication) context.getApplicationContext()).team;

        final DynamicRealmObject person = update.getObject(Thing.PERSON);
        final DynamicRealmObject location = !update.getObject(Thing.TARGET).isNull(Thing.LOCATION) ?
                update.getObject(Thing.TARGET).getObject(Thing.LOCATION) : null;

        if(person != null) {
            int s = (int) Util.px(64);

            ImageView profile = (ImageView) view.findViewById(R.id.profile);

            Picasso.with(context)
                    .load(location == null ? Functions.getImageUrlForSize(person, s) : Util.locationPhoto(location, s))
                    .placeholder(location == null ? R.color.spacer : R.drawable.location)
                    .into(profile);

            profile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    team.action.openProfile((Activity) context, person);
                }
            });

            TextView type = (TextView) view.findViewById(R.id.type);
            type.setText(context.getString(R.string.person_posted, person.getString(Thing.FIRST_NAME)));

            TextView time = (TextView) view.findViewById(R.id.time);
            time.setText(TimeUtil.agoDate(update.getDate(Thing.DATE)));
        }

        view.setClickable(true);
        view.setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(team.context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    team.action.likeUpdate(update);
                    updateLikes(view, update, context);
                    return true;
                }
            });

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });

        updateLikes(view, update, context);

        ImageView photo = (ImageView) view.findViewById(R.id.photo);

        if (Config.UPDATE_ACTION_UPTO.equals(update.getString(Thing.ACTION))) {
            if (update.getBoolean(Thing.PHOTO)) {
                photo.setVisibility(View.VISIBLE);

                Util.setPhotoWithPicasso(update, parent.getMeasuredWidth(), photo);
            } else {
                photo.setVisibility(View.GONE);
            }

            if(update.isNull(Thing.ABOUT) || update.getString(Thing.ABOUT).isEmpty()) {
                view.findViewById(R.id.details).setVisibility(View.GONE);
            }
            else {
                view.findViewById(R.id.details).setVisibility(View.VISIBLE);
            }
        }
        else {
            view.findViewById(R.id.details).setVisibility(View.VISIBLE);
            view.findViewById(R.id.likers).setVisibility(View.GONE);
            photo.setVisibility(View.GONE);
        }

        ((TextView) view.findViewById(R.id.details)).setText(
                Util.getUpdateText(update)
        );

        return view;
    }

    private void updateLikes(final View view, final DynamicRealmObject update, final Context context) {
        final Team team = ((MainApplication) context.getApplicationContext()).team;

        final Button likers = (Button) view.findViewById(R.id.likers);

        int likersCount = (int) team.realm.where("Thing")
                .equalTo(Thing.KIND, "like")
                .equalTo("target.id", update.getString(Thing.ID))
                .count();

        if (update.getInt(Thing.LIKERS) > likersCount) {
            likersCount = update.getInt(Thing.LIKERS);
        }

        boolean byMe = Util.liked(update, team.auth.me());

        likers.setText(team.context.getResources().getQuantityString(byMe ? R.plurals.likes_me : R.plurals.likes, likersCount, likersCount));
        likers.setVisibility(likersCount > 0 ? View.VISIBLE : View.GONE);

        if (likersCount > 0) {
            likers.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    team.action.showLikers((Activity) context, update);
                }
            });
        }

//        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) likers.getLayoutParams();
//
//        if (params == null) {
//            params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        }
//
//        if (View.GONE == view.findViewById(R.id.details).getVisibility()) {
//            params.bottomMargin = 0;
//        } else {
//            params.bottomMargin = (int) Util.px(-8);
//        }

//        likers.setLayoutParams(params);
    }
}
