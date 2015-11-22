package com.queatz.snappy.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.queatz.snappy.MainApplication;
import com.queatz.snappy.R;
import com.queatz.snappy.Util;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.things.Offer;

import io.realm.RealmBaseAdapter;
import io.realm.RealmResults;

/**
 * Created by jacob on 8/29/15.
 */
public class OfferAdapter extends RealmBaseAdapter<Offer> {
    public OfferAdapter(Context context, RealmResults<Offer> realmResults) {
        super(context, realmResults, true);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;

        if (convertView != null) {
            view = convertView;
        }
        else {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.offer_item, parent, false);
        }

        Team team = ((MainApplication) context.getApplicationContext()).team;

        Offer offer = realmResults.get(position);

        TextView details = (TextView) view.findViewById(R.id.details);
        TextView price = (TextView) view.findViewById(R.id.price);

        details.setText(offer.getDetails());
        price.setText(offer.getPrice() > 0 ? Util.offerAmount(offer) : offer.getPrice() < 0 ? (team.auth.getUser().equals(offer.getPerson().getId()) ? "-" : "+") + Util.offerAmount(offer) : context.getString(R.string.free));

        if (offer.getPrice() < 0) {
            view.setBackgroundResource(R.color.purple);
        } else {
            view.setBackgroundResource(R.color.green);
        }

        if(team.auth.getUser() != null && team.auth.getUser().equals(offer.getPerson().getId())) {
            view.setTag(offer);
            ((Activity) context).registerForContextMenu(view);
        }

        return view;
    }
}
