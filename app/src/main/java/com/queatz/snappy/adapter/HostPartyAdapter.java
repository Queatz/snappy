package com.queatz.snappy.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.queatz.snappy.R;
import com.queatz.snappy.Util;
import com.queatz.snappy.things.Party;

import io.realm.RealmBaseAdapter;
import io.realm.RealmResults;

/**
 * Created by jacob on 2/16/15.
 */
public class HostPartyAdapter extends RealmBaseAdapter<Party> {
    public HostPartyAdapter(Context context, RealmResults<Party> realmResults) {
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
            view = inflater.inflate(R.layout.host_party_again, parent, false);
        }

        Party party = realmResults.get(position);

        ((TextView) view.findViewById(R.id.name)).setText(party.getName());
        ((TextView) view.findViewById(R.id.ago)).setText(Util.cuteDate(context, party.getDate()));

        return view;
    }
}
