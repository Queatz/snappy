package com.queatz.snappy.ui.card;

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
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.things.Offer;
import com.squareup.picasso.Picasso;

/**
 * Created by jacob on 11/12/15.
 */
public class OfferCard implements Card<Offer> {
    @Override
    public View getCard(final Context context, final Offer offer, View convertView, ViewGroup parent) {
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

        details.setText(offer.getDetails());
        takeOffer.setText(Util.offerPriceText(offer));

        final Team team = ((MainApplication) context.getApplicationContext()).team;

        if(team.auth.getUser() != null && team.auth.getUser().equals(offer.getPerson().getId())) {
            view.setTag(offer);
            ((Activity) context).registerForContextMenu(view);
        }

        ImageView profile = (ImageView) view.findViewById(R.id.profile);
        Picasso.with(context).load(offer.getPerson().getImageUrlForSize((int) Util.px(64))).placeholder(R.color.spacer).into(profile);
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                team.action.openProfile((Activity) context, offer.getPerson());
            }
        });

        View.OnClickListener onClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, team.context.getString(R.string.opening_conversation), Toast.LENGTH_SHORT).show();
                team.action.openMessages((Activity) context, offer.getPerson(), Util.offerMessagePrefill(offer));
            }
        };

        view.findViewById(R.id.takeOffer).setOnClickListener(onClick);
        view.setOnClickListener(onClick);

        TextView type = (TextView) view.findViewById(R.id.type);

        if (Util.offerIsRequest(offer)) {
            type.setText(context.getString(R.string.person_wants, offer.getPerson().getFirstName()));
            type.setTextColor(context.getResources().getColor(R.color.purple));
        } else {
            type.setText(context.getString(R.string.person_offers, offer.getPerson().getFirstName()));
            type.setTextColor(context.getResources().getColor(R.color.green));
        }

        int colorResource = (Util.offerIsRequest(offer) ? R.color.purple : R.color.green);
        view.findViewById(R.id.highlight).setBackgroundResource(colorResource);
        ((Button) view.findViewById(R.id.takeOffer)).setTextColor(context.getResources().getColor(colorResource));

        ImageView photo = (ImageView) view.findViewById(R.id.photo);

        if (offer.isHasPhoto()) {
            String photoUrl = Util.photoUrl(String.format(Config.PATH_OFFER_PHOTO, offer.getId()), parent.getMeasuredWidth() / 2);

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
