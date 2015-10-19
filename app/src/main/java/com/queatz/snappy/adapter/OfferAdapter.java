package com.queatz.snappy.adapter;

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

        Offer offer = realmResults.get(position);

        TextView details = (TextView) view.findViewById(R.id.details);
        TextView price = (TextView) view.findViewById(R.id.price);

        details.setText(offer.getDetails());
        price.setText(offer.getPrice() > 0 ? "$" + offer.getPrice() : context.getString(R.string.free));

        Team team = ((MainApplication) context.getApplicationContext()).team;

        if(team.auth.getUser() != null && team.auth.getUser().equals(offer.getPerson().getId())) {
            view.setTag(offer);
            ((Activity) context).registerForContextMenu(view);
        }

        return view;
    }
}
