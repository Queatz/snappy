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
import android.widget.TextView;
import android.widget.Toast;

import com.queatz.snappy.MainApplication;
import com.queatz.snappy.R;
import com.queatz.snappy.Util;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.team.Thing;
import com.queatz.snappy.util.Functions;
import com.squareup.picasso.Picasso;

import io.realm.DynamicRealmObject;

/**
 * Created by jacob on 11/12/15.
 */
public class OfferCard implements Card<DynamicRealmObject> {
    @Override
    public View getCard(final Context context, final DynamicRealmObject offer, View convertView, ViewGroup parent) {
        View view;

        if (convertView != null) {
            view = convertView;
        }
        else {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.offer_card, parent, false);
        }

        view.setTag(offer);

        TextView details = (TextView) view.findViewById(R.id.details);
        Button takeOffer = (Button) view.findViewById(R.id.takeOffer);

        details.setText(offer.getString(Thing.ABOUT));
        takeOffer.setText(Util.offerPriceText(offer));

        final Team team = ((MainApplication) context.getApplicationContext()).team;

        if(team.auth.getUser() != null && team.auth.getUser().equals(offer.getObject(Thing.PERSON).getString(Thing.ID))) {
            view.setTag(offer);
            ((Activity) context).registerForContextMenu(view);
        }

        ImageView profile = (ImageView) view.findViewById(R.id.profile);
        Picasso.with(context).load(Functions.getImageUrlForSize(offer.getObject(Thing.PERSON), (int) Util.px(64))).placeholder(R.color.spacer).into(profile);
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                team.action.openProfile((Activity) context, offer.getObject(Thing.PERSON));
            }
        });

        View.OnClickListener onClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Messages are settings for the active person, so skip
                if (offer.getObject(Thing.PERSON).equals(team.auth.me())) {
                    return;
                }

                Toast.makeText(context, team.context.getString(R.string.opening_conversation), Toast.LENGTH_SHORT).show();
                team.action.openMessages((Activity) context, offer.getObject(Thing.PERSON), Util.offerMessagePrefill(offer));
            }
        };

        view.findViewById(R.id.takeOffer).setOnClickListener(onClick);
        view.setOnClickListener(onClick);

        view.setClickable(true);
        view.setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(team.context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    team.action.likeUpdate(offer);
                    return true;
                }
            });

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });

        TextView type = (TextView) view.findViewById(R.id.type);

        if (Util.offerIsRequest(offer)) {
            type.setText(context.getString(R.string.person_wants, offer.getObject(Thing.PERSON).getString(Thing.FIRST_NAME)));
            type.setTextColor(context.getResources().getColor(R.color.purple));
        } else {
            type.setText(context.getString(R.string.person_offers, offer.getObject(Thing.PERSON).getString(Thing.FIRST_NAME)));
            type.setTextColor(context.getResources().getColor(R.color.green));
        }

        int colorResource = (Util.offerIsRequest(offer) ? R.color.purple : R.color.green);
        view.findViewById(R.id.highlight).setBackgroundResource(colorResource);
        ((Button) view.findViewById(R.id.takeOffer)).setTextColor(context.getResources().getColor(colorResource));

        ImageView photo = (ImageView) view.findViewById(R.id.photo);

        if (offer.getBoolean(Thing.PHOTO)) {
            String photoUrl = Util.photoUrl(Config.PATH_EARTH + "/" + offer.getString(Thing.ID) + "/" + Config.PATH_PHOTO, parent.getMeasuredWidth() / 2);

            photo.setVisibility(View.VISIBLE);
            photo.setImageDrawable(null);

            Picasso.with(context).cancelRequest(photo);

            Picasso.with(context)
                    .load(photoUrl)
                    .placeholder(R.color.spacer)
                    .into(photo);
        } else {
            photo.setVisibility(View.GONE);
        }

        TextView likers = (TextView) view.findViewById(R.id.likers);

        int likersCount = (int) team.realm.where("Thing")
                .equalTo("target.id", offer.getString(Thing.ID))
                .count();

        // XXX How to do dees
        if (offer.getInt(Thing.LIKERS) > likersCount) {
            likersCount = offer.getInt(Thing.LIKERS);
        }

        likers.setText(team.context.getResources().getQuantityString(R.plurals.likes_count_me, likersCount, likersCount));
        likers.setVisibility(likersCount > 0 ? View.VISIBLE : View.GONE);

        if (likersCount > 0) {
            likers.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    team.action.showLikers((Activity) context, offer);
                }
            });
        }


        return view;
    }
}
