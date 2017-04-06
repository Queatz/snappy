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

import com.queatz.branch.Branch;
import com.queatz.snappy.MainApplication;
import com.queatz.snappy.R;
import com.queatz.snappy.Util;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.team.Thing;
import com.queatz.snappy.team.actions.LikeUpdateAction;
import com.queatz.snappy.team.actions.OpenProfileAction;
import com.queatz.snappy.team.contexts.ActivityContext;
import com.queatz.snappy.util.Functions;
import com.queatz.snappy.util.TimeUtil;
import com.squareup.picasso.Picasso;

import io.realm.DynamicRealmObject;

/**
 * Created by jacob on 11/12/15.
 */
public class OfferCard implements Card<DynamicRealmObject> {

    @Override
    public View getCard(final Context context, final DynamicRealmObject offer, View convertView, ViewGroup parent) {
        final Branch<ActivityContext> branch = Branch.from((ActivityContext) context);
        final View view;


        if (convertView != null) {
            view = convertView;
        } else {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.offer_card, parent, false);
        }

        view.setTag(offer);

        TextView details = (TextView) view.findViewById(R.id.details);
        Button takeOffer = (Button) view.findViewById(R.id.takeOffer);

        details.setText(offer.getString(Thing.ABOUT));
        takeOffer.setText(Util.offerPriceText(offer));

        final Team team = ((MainApplication) context.getApplicationContext()).team;

        if (offer.getObject(Thing.PERSON) != null) {

            if (team.auth.getUser() != null && team.auth.getUser().equals(offer.getObject(Thing.PERSON).getString(Thing.ID))) {
                view.setTag(offer);
                ((Activity) context).registerForContextMenu(view);
            }

            ImageView profile = (ImageView) view.findViewById(R.id.profile);
            Picasso.with(context)
                    .load(Functions.getImageUrlForSize(offer.getObject(Thing.PERSON), (int) Util.px(64)))
                    .placeholder(R.color.spacer)
                    .into(profile);
            profile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    branch.to(new OpenProfileAction(offer.getObject(Thing.PERSON)));
                }
            });

            final View.OnClickListener onClick = new View.OnClickListener() {
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

            view.setClickable(true);
            view.setOnTouchListener(new View.OnTouchListener() {
                private GestureDetector gestureDetector = new GestureDetector(team.context, new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onDoubleTap(MotionEvent e) {
                        branch.to(new LikeUpdateAction(offer));
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

        }

        TextView time = (TextView) view.findViewById(R.id.time);

        if (!offer.isNull(Thing.DATE)) {
            time.setVisibility(View.VISIBLE);
            time.setText(TimeUtil.agoDate(offer.getDate(Thing.DATE)));
        } else {
            time.setVisibility(View.GONE);
        }

        int colorResource = (Util.offerIsRequest(offer) ? R.color.purple : R.color.green);
        view.findViewById(R.id.highlight).setBackgroundResource(colorResource);
        ((Button) view.findViewById(R.id.takeOffer)).setTextColor(context.getResources().getColor(colorResource));

        final ImageView photo = (ImageView) view.findViewById(R.id.photo);

        if (offer.getBoolean(Thing.PHOTO)) {
            photo.setVisibility(View.VISIBLE);
            Util.setPhotoWithPicasso(offer, parent.getMeasuredWidth(), photo);
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

        if (likersCount > 0) {
            likers.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_favorite_white_24dp, 0, 0, 0);
            likers.setText(team.context.getResources().getQuantityString(R.plurals.likes_count_me, likersCount, likersCount));
            likers.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    team.action.showLikers((Activity) context, offer);
                }
            });
        } else {
            likers.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_favorite_border_white_24dp, 0, 0, 0);
            likers.setText("");
            likers.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    branch.to(new LikeUpdateAction(offer));
                }
            });
        }

        likers.getCompoundDrawables()[0].setTint(context.getResources().getColor(R.color.red));

        Button shareButton = (Button) view.findViewById(R.id.shareButton);

        shareButton.getCompoundDrawables()[0].setTint(context.getResources().getColor(R.color.gray));

        view.findViewById(R.id.shareButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                team.action.share((Activity) view.getContext(), offer);
            }
        });
        return view;
    }
}
