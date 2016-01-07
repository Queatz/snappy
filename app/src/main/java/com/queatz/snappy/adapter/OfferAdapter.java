package com.queatz.snappy.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.queatz.snappy.MainApplication;
import com.queatz.snappy.R;
import com.queatz.snappy.Util;
import com.queatz.snappy.fragment.PersonMessagesSlide;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.things.Endorsement;
import com.queatz.snappy.things.Like;
import com.queatz.snappy.things.Offer;
import com.queatz.snappy.ui.SlideScreen;

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

        final Offer offer = realmResults.get(position);

        TextView details = (TextView) view.findViewById(R.id.details);
        TextView price = (TextView) view.findViewById(R.id.price);

        details.setText(offer.getDetails());
        price.setText(offer.getPrice() > 0 ? Util.offerAmount(offer) : offer.getPrice() < 0 ? "+" + Util.offerAmount(offer) : context.getString(R.string.free));

        if (offer.getPrice() < 0) {
            price.setTextColor(context.getResources().getColor(R.color.purple));
        } else {
            price.setTextColor(context.getResources().getColor(R.color.green));
        }

        boolean itsMe = team.auth.getUser() != null && team.auth.getUser().equals(offer.getPerson().getId());

        if(itsMe) {
            view.setTag(offer);
            ((Activity) context).registerForContextMenu(view);
        } else {
            price.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context, team.context.getString(R.string.opening_conversation), Toast.LENGTH_SHORT).show();
                    SlideScreen slideScreen = (SlideScreen) ((Activity) context).findViewById(R.id.person_content);
                    ((PersonMessagesSlide) slideScreen.getSlideFragment(1)).setMessagePrefill((offer.getPrice() < 0 ? "I've got " : "I'd like ") + offer.getDetails());
                    slideScreen.setSlide(1);
                }
            });
        }

        Button endorsements = (Button) view.findViewById(R.id.endorsements);

        int endorsers = (int) team.realm.where(Endorsement.class)
                .equalTo("target.id", offer.getId())
                .count();

        if (offer.getEndorsers() > endorsers) {
            endorsers = offer.getEndorsers();
        }

        if (itsMe) {
            if (endorsers > 0) {
                endorsements.setVisibility(View.VISIBLE);
                endorsements.setText(Integer.toString(endorsers));
            } else {
                endorsements.setVisibility(View.GONE);
            }
        } else {
            endorsements.setText(endorsers == 0 ? context.getString(R.string.endorse) : Integer.toString(endorsers));
            endorsements.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    team.action.endorse((Activity) context, offer);
                }
            });
        }

        endorsements.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                team.action.showEndorsers((Activity) context, offer);

                return true;
            }
        });


        return view;
    }
}
