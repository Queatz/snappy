package com.queatz.snappy.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.queatz.snappy.MainApplication;
import com.queatz.snappy.R;
import com.queatz.snappy.Util;
import com.queatz.snappy.fragment.PersonMessagesSlide;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.team.Thing;
import com.queatz.snappy.ui.SlideScreen;
import com.squareup.picasso.Picasso;

import io.realm.DynamicRealmObject;
import io.realm.RealmBaseAdapter;
import io.realm.RealmResults;

/**
 * Created by jacob on 8/29/15.
 */
public class OfferAdapter extends RealmBaseAdapter<DynamicRealmObject> {
    public OfferAdapter(Context context, RealmResults<DynamicRealmObject> realmResults) {
        super(context, realmResults);
    }

    @Override
    public View getView(int position, final View convertView, ViewGroup parent) {
        View view;

        if (convertView != null) {
            view = convertView;
        }
        else {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.offer_item, parent, false);
        }

        final Team team = ((MainApplication) context.getApplicationContext()).team;

        final DynamicRealmObject offer = getItem(position);

        TextView details = (TextView) view.findViewById(R.id.details);
        TextView price = (TextView) view.findViewById(R.id.price);

        details.setText(offer.getString(Thing.ABOUT));
        price.setText(Util.offerPriceText(offer, true));

        if (Util.offerIsRequest(offer)) {
            price.setTextColor(context.getResources().getColor(R.color.purple));
        } else {
            price.setTextColor(context.getResources().getColor(R.color.green));
        }

        boolean itsMe = team.auth.getUser() != null && team.auth.getUser().equals(offer.getObject(Thing.PERSON).getString(Thing.ID));

        if(itsMe) {
            view.setTag(offer);
            ((Activity) context).registerForContextMenu(view);
        } else {
            price.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context, team.context.getString(R.string.opening_conversation), Toast.LENGTH_SHORT).show();
                    SlideScreen slideScreen = (SlideScreen) ((Activity) context).findViewById(R.id.person_content);
                    ((PersonMessagesSlide) slideScreen.getSlideFragment(1)).setMessagePrefill(Util.offerMessagePrefill(offer));
                    slideScreen.setSlide(1);
                }
            });
        }

        Button likesButton = (Button) view.findViewById(R.id.likers);

        int likers = (int) team.realm.where("Thing")
                .equalTo(Thing.KIND, "like")
                .equalTo("target.id", offer.getString(Thing.ID))
                .count();

        if (offer.getInt(Thing.LIKERS) > likers) {
            likers = offer.getInt(Thing.LIKERS);
        }

        if (itsMe) {
            if (likers > 0) {
                likesButton.setVisibility(View.VISIBLE);
                likesButton.setText(Integer.toString(likers));
            } else {
                likesButton.setVisibility(View.GONE);
            }
        } else {
            likesButton.setText(likers == 0 ? context.getString(R.string.like) : Integer.toString(likers));

            if (likers == 0) {
                likesButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //team.action.like((Activity) context, offer);
                    }
                });
            } else {
                likesButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                       // team.action.showLikers((Activity) context, offer);
                    }
                });
            }
        }

        likesButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
               // team.action.like((Activity) context, offer);

                return true;
            }
        });

        ImageView photo = (ImageView) view.findViewById(R.id.photo);

        if (offer.getBoolean(Thing.PHOTO)) {
            String photoUrl = Util.photoUrl(String.format(Config.PATH_OFFER_PHOTO, offer.getString(Thing.ID)), parent.getMeasuredWidth() / 2);

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

        return view;
    }
}
