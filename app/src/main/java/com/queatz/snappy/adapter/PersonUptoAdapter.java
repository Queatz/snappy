package com.queatz.snappy.adapter;

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
import com.squareup.picasso.Picasso;

import io.realm.DynamicRealmObject;
import io.realm.RealmBaseAdapter;
import io.realm.RealmResults;

/**
 * Created by jacob on 2/18/15.
 */
public class PersonUptoAdapter extends RealmBaseAdapter<DynamicRealmObject> {
    public PersonUptoAdapter(Context context, RealmResults<DynamicRealmObject> realmResults) {
        super(context, realmResults);
    }

    public void updateLikes(View view, final DynamicRealmObject update) {
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
                    team.action.showLikers((Activity) PersonUptoAdapter.this.context, update);
                }
            });
        }

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) likers.getLayoutParams();

        if (params == null) {
            params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        if (View.GONE == view.findViewById(R.id.details).getVisibility()) {
            params.bottomMargin = 0;
        } else {
            params.bottomMargin = (int) Util.px(-8);
        }

        likers.setLayoutParams(params);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final View view;

        if (convertView != null) {
            view = convertView;
        }
        else {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.person_upto_item, parent, false);
        }

        final Team team = ((MainApplication) context.getApplicationContext()).team;

        final DynamicRealmObject update = getItem(position);
        final DynamicRealmObject person = update.getObject(Thing.PERSON);
        final DynamicRealmObject location = !update.getObject(Thing.TARGET).isNull(Thing.LOCATION) ?
                update.getObject(Thing.TARGET).getObject(Thing.LOCATION) : null;

        if(person != null) {
            int s = (int) Util.px(64);

            Picasso.with(context)
                    .load(location == null ? Functions.getImageUrlForSize(person, s) : Util.locationPhoto(location, s))
                    .placeholder(location == null ? R.color.spacer : R.drawable.location)
                    .into((ImageView) view.findViewById(R.id.profile));
        }

        ImageView photo = (ImageView) view.findViewById(R.id.photo);

        if (Config.UPDATE_ACTION_UPTO.equals(update.getString(Thing.ACTION))) {
            String photoUrl = Util.photoUrl(String.format(Config.PATH_EARTH_PHOTO, update.getString(Thing.ID)), parent.getMeasuredWidth() / 2);

            photo.setImageDrawable(null);
            photo.setVisibility(View.VISIBLE);

            Picasso.with(context).cancelRequest(photo);

            Picasso.with(context)
                    .load(photoUrl)
                    .placeholder(R.color.spacer)
                    .into(photo);

            if(update.isNull(Thing.ABOUT) || update.getString(Thing.ABOUT).isEmpty()) {
                view.findViewById(R.id.details).setVisibility(View.GONE);
            }
            else {
                view.findViewById(R.id.details).setVisibility(View.VISIBLE);
            }

            photo.setClickable(true);
            photo.setOnTouchListener(new View.OnTouchListener() {
                private GestureDetector gestureDetector = new GestureDetector(team.context, new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onDoubleTap(MotionEvent e) {
                        team.action.likeUpdate(update);
                        updateLikes(view, update);
                        return true;
                    }
                });

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return gestureDetector.onTouchEvent(event);
                }
            });

            updateLikes(view, update);
        }
        else {
            view.findViewById(R.id.details).setVisibility(View.VISIBLE);
            view.findViewById(R.id.likers).setVisibility(View.GONE);
            photo.setVisibility(View.GONE);
        }

        ((TextView) view.findViewById(R.id.text)).setText(
                Util.getUpdateText(update)
        );

        return view;
    }
}
