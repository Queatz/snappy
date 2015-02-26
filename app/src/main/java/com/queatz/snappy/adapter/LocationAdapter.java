package com.queatz.snappy.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.queatz.snappy.MainApplication;
import com.queatz.snappy.R;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.things.Location;

import io.realm.RealmBaseAdapter;
import io.realm.RealmResults;

/**
 * Created by jacob on 2/25/15.
 */
public class LocationAdapter extends RealmBaseAdapter<Location> {
    private int mLimit = -1;

    public LocationAdapter(Context context, RealmResults<Location> realmResults, int limit) {
        super(context, realmResults, true);
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

        Location location = realmResults.get(position);

        TextView name = (TextView) view.findViewById(R.id.name);
        ImageView profile = (ImageView) view.findViewById(R.id.profile);

        /*Picasso.with(context)
                .load(location.getImageUrlForSize((int) Util.px(64)))
                .placeholder(R.color.spacer)
                .into(profile);*/

        name.setText(location.getName());

        return view;
    }
}
