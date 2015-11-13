package com.queatz.snappy.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.queatz.snappy.MainApplication;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.things.Offer;
import com.queatz.snappy.things.Party;
import com.queatz.snappy.things.Quest;
import com.queatz.snappy.ui.card.OfferCard;
import com.queatz.snappy.ui.card.PartyCard;
import com.queatz.snappy.ui.card.QuestCard;

import java.util.List;

import io.realm.RealmChangeListener;
import io.realm.RealmObject;
import io.realm.RealmResults;

/**
 * Created by jacob on 11/12/15.
 */
public class FeedAdapter extends BaseAdapter {
    protected List<RealmResults> results;
    protected Context context;
    private final RealmChangeListener listener;

    private QuestCard questCard = new QuestCard();
    private PartyCard partyCard = new PartyCard();
    private OfferCard offerCard = new OfferCard();

    public FeedAdapter(Context context, List<RealmResults> results) {
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }

        this.context = context;
        this.results = results;

        this.listener = new RealmChangeListener() {
            @Override
            public void onChange() {
                notifyDataSetChanged();
            }
        };

        final Team team = ((MainApplication) context.getApplicationContext()).team;

        team.realm.addChangeListener(listener);
    }

    @Override
    public int getCount() {
        int count = 0;

        for (RealmResults realmResults : results) {
            count += realmResults.size();
        }

        return count;
    }

    @Override
    public RealmObject getItem(int i) {
        for (RealmResults realmResults : results) {
            if (i < realmResults.size()) {
                return realmResults.get(i);
            }

            i -= realmResults.size();
        }

        return null;
    }

    @Override
    public long getItemId(int i) {
        // TODO: find better solution once we have unique IDs
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        RealmObject realmObject = getItem(position);

        if (realmObject instanceof Quest) {
            view = questCard.getCard(context, (Quest) realmObject, convertView != null && convertView.getTag() instanceof Quest ? convertView : null, parent);
        } else if (realmObject instanceof Party) {
            view = partyCard.getCard(context, (Party) realmObject, convertView != null && convertView.getTag() instanceof Party ? convertView : null, parent);
        }  else if (realmObject instanceof Offer) {
            view = offerCard.getCard(context, (Offer) realmObject, convertView != null && convertView.getTag() instanceof Offer ? convertView : null, parent);
        } else {
            throw new IllegalStateException("Invalid object found: " + realmObject);
        }

        return view;
    }
}