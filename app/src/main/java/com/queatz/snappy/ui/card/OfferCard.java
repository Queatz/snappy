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
        takeOffer.setText(offer.getPrice() > 0 ?
                context.getString(R.string.for_amount, Util.offerAmount(offer)) : offer.getPrice() < 0 ?
                context.getString(R.string.make_amount, Util.offerAmount(offer)) :
                context.getString(R.string.for_free));

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
                team.action.openMessages((Activity) context, offer.getPerson(), (offer.getPrice() < 0 ? "I've got " : "I'd like ") + offer.getDetails());
            }
        };

        view.findViewById(R.id.takeOffer).setOnClickListener(onClick);
        view.setOnClickListener(onClick);

        TextView type = (TextView) view.findViewById(R.id.type);

        if (offer.getPrice() < 0) {
            type.setText(context.getString(R.string.person_wants, offer.getPerson().getFirstName()));
            type.setTextColor(context.getResources().getColor(R.color.purple));
        } else {
            type.setText(context.getString(R.string.person_offers, offer.getPerson().getFirstName()));
            type.setTextColor(context.getResources().getColor(R.color.green));
        }


        int colorResource = (offer.getPrice() < 0 ? R.color.purple : R.color.green);
        view.findViewById(R.id.highlight).setBackgroundResource(colorResource);
        ((Button) view.findViewById(R.id.takeOffer)).setTextColor(context.getResources().getColor(colorResource));

        return view;
    }
}
