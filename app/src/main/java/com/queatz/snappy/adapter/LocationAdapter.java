package com.queatz.snappy.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.queatz.snappy.MainApplication;
import com.queatz.snappy.R;
import com.queatz.snappy.Util;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.team.Thing;
import com.squareup.picasso.Picasso;

import io.realm.DynamicRealmObject;
import io.realm.RealmBaseAdapter;
import io.realm.RealmResults;

/**
 * Created by jacob on 2/25/15.
 */
public class LocationAdapter extends RealmBaseAdapter<DynamicRealmObject> {
    private final Context context;
    private int mLimit = -1;

    public LocationAdapter(Context context, RealmResults<DynamicRealmObject> realmResults, int limit) {
        super(realmResults);
        this.context = context;
        mLimit = limit;
    }

    @Override
    public int getCount() {
        if(mLimit >= 0)
            return Math.min(super.getCount(), mLimit);

        return super.getCount();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;

        final Team team = ((MainApplication) context.getApplicationContext()).team;

        if (convertView != null) {
            view = convertView;
        }
        else {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.location_item, parent, false);
        }

        DynamicRealmObject location = getItem(position);

        TextView name = (TextView) view.findViewById(R.id.name);
        ImageView profile = (ImageView) view.findViewById(R.id.profile);

        int s = (int) Util.px(128);
        String photoUrl = Config.API_URL + String.format(Config.PATH_EARTH_PHOTO + "?s=" + s + "&auth=" + team.auth.getAuthParam(), location.getString(Thing.ID));

        Picasso.with(context).load(photoUrl).placeholder(R.drawable.location).into(profile);

        name.setText(location.getString(Thing.NAME));

        return view;
    }
}
