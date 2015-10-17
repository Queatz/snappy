package com.queatz.snappy.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.queatz.snappy.shared.Config;
import com.queatz.snappy.MainApplication;
import com.queatz.snappy.R;
import com.queatz.snappy.Util;
import com.queatz.snappy.things.Party;
import com.queatz.snappy.util.TimeUtil;
import com.squareup.picasso.Picasso;

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
            view = inflater.inflate(R.layout.host_party_item, parent, false);
        }

        Party party = realmResults.get(position);

        ((TextView) view.findViewById(R.id.name)).setText(party.getName());
        ((TextView) view.findViewById(R.id.ago)).setText(TimeUtil.agoDate(party.getDate()));

        int s = (int) Util.px(128);
        String photoUrl = Config.API_URL + String.format(Config.PATH_LOCATION_PHOTO + "?s=" + s + "&auth=" + ((MainApplication) context.getApplicationContext()).team.auth.getAuthParam(), party.getLocation().getId());

        ImageView locationIcon = (ImageView) view.findViewById(R.id.location_icon);
        Picasso.with(context).load(photoUrl).placeholder(R.drawable.location).into(locationIcon);

        return view;
    }
}
