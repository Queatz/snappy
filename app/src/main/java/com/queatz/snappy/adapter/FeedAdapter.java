package com.queatz.snappy.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.queatz.snappy.MainApplication;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.team.Thing;
import com.queatz.snappy.ui.card.OfferCard;
import com.queatz.snappy.ui.card.PartyCard;
import com.queatz.snappy.ui.card.UpdateCard;

import java.util.List;

import io.realm.DynamicRealm;
import io.realm.DynamicRealmObject;
import io.realm.RealmChangeListener;
import io.realm.RealmModel;
import io.realm.RealmObject;
import io.realm.RealmResults;

/**
 * Created by jacob on 11/12/15.
 */
public class FeedAdapter extends BaseAdapter {
    protected List<RealmResults> results;
    protected Context context;
    private final RealmChangeListener<DynamicRealm> listener;

    private UpdateCard updateCard = new UpdateCard();
    private PartyCard partyCard = new PartyCard();
    private OfferCard offerCard = new OfferCard();

    public FeedAdapter(Context context, List<RealmResults> results) {
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }

        this.context = context;
        this.results = results;

        this.listener = new RealmChangeListener<DynamicRealm>() {
            @Override
            public void onChange(DynamicRealm realm) {
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
    public RealmModel getItem(int i) {
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
        DynamicRealmObject thing = (DynamicRealmObject) getItem(position);

        boolean isSameKind = convertView != null &&
                ((DynamicRealmObject) convertView.getTag()).isValid() &&
                thing.getString(Thing.KIND).equals(((DynamicRealmObject) convertView.getTag()).getString(Thing.KIND));

        if ("party".equals(thing.getString(Thing.KIND))) {
            view = partyCard.getCard(context, thing, isSameKind ? convertView : null, parent);
            view.setTag(thing);
        }  else if ("offer".equals(thing.getString(Thing.KIND))) {
            view = offerCard.getCard(context, thing, isSameKind ? convertView : null, parent);
            view.setTag(thing);
        }  else if ("update".equals(thing.getString(Thing.KIND))) {
            view = updateCard.getCard(context, thing, isSameKind ? convertView : null, parent);
            view.setTag(thing);
        } else {
            throw new IllegalStateException("Invalid object found: " + thing);
        }

        return view;
    }
}