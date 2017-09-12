package com.queatz.snappy.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.queatz.snappy.MainApplication;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.team.Thing;
import com.queatz.snappy.team.ThingKinds;
import com.queatz.snappy.ui.card.OfferCard;
import com.queatz.snappy.ui.card.PartyCard;
import com.queatz.snappy.ui.card.UpdateCard;

import java.util.ArrayList;
import java.util.List;

import io.realm.DynamicRealm;
import io.realm.DynamicRealmObject;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

/**
 * Created by jacob on 11/12/15.
 */
public class FeedAdapter extends BaseAdapter {
    private final List<RealmResults<DynamicRealmObject>> results;
    private Context context;

    private UpdateCard updateCard = new UpdateCard();
    private PartyCard partyCard = new PartyCard();
    private OfferCard offerCard = new OfferCard();

    public FeedAdapter(Context context, ArrayList<RealmResults<DynamicRealmObject>> results) {
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }

        this.context = context;
        this.results = results;

        final Team team = ((MainApplication) context.getApplicationContext()).team;

        team.realm.addChangeListener(new RealmChangeListener<DynamicRealm>() {
            @Override
            public void onChange(DynamicRealm realm) {
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getCount() {
        int count = 0;

        for (Object result : results) {
            if (result instanceof RealmResults) {
                count += ((RealmResults) result).size();
            } else {
                count += 1;
            }
        }

        return count;
    }

    @Override
    public Object getItem(int i) {
        for (Object result : results) {
            if (result instanceof RealmResults) {
                RealmResults realmResults = (RealmResults) result;

                if (i < 0) {
                    return result;
                } else if (i < realmResults.size()) {
                    return realmResults.get(i);
                }

                i -= realmResults.size();
            } else {
                i -= 1;
            }
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

        if (ThingKinds.PARTY.equals(thing.getString(Thing.KIND))) {
            view = partyCard.getCard(context, thing, isSameKind ? convertView : null, parent);
            view.setTag(thing);
        }  else if (ThingKinds.OFFER.equals(thing.getString(Thing.KIND))) {
            view = offerCard.getCard(context, thing, isSameKind ? convertView : null, parent);
            view.setTag(thing);
        }  else if (ThingKinds.UPDATE.equals(thing.getString(Thing.KIND))) {
            view = updateCard.getCard(context, thing, isSameKind ? convertView : null, parent);
            view.setTag(thing);
        } else {
            throw new IllegalStateException("Invalid object found: " + thing);
        }

        return view;
    }
}