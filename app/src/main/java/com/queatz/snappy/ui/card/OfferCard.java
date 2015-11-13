package com.queatz.snappy.ui.card;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.queatz.snappy.MainApplication;
import com.queatz.snappy.R;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.things.Offer;

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
        TextView price = (TextView) view.findViewById(R.id.price);

        details.setText(offer.getDetails());
        price.setText(offer.getPrice() > 0 ? "$" + offer.getPrice() : context.getString(R.string.free));

        final Team team = ((MainApplication) context.getApplicationContext()).team;

        if(team.auth.getUser() != null && team.auth.getUser().equals(offer.getPerson().getId())) {
            view.setTag(offer);
            ((Activity) context).registerForContextMenu(view);
        }

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                team.action.openMessages((Activity) context, offer.getPerson());
            }
        });

        return view;
    }
}
