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
import com.queatz.snappy.team.Team;
import com.queatz.snappy.team.Thing;
import com.queatz.snappy.util.Functions;
import com.squareup.picasso.Picasso;

import io.realm.DynamicRealmObject;
import io.realm.RealmBaseAdapter;
import io.realm.RealmResults;

/**
 * Created by jacob on 2/19/15.
 */
public class PersonListAdapter extends RealmBaseAdapter<DynamicRealmObject> {
    private final Context context;
    boolean showFollowing;

    public PersonListAdapter(Context context, RealmResults<DynamicRealmObject> realmResults, boolean showFollowing) {
        super(realmResults);
        this.context = context;
        this.showFollowing = showFollowing;
    }

    public DynamicRealmObject getPerson(int position) {
        DynamicRealmObject follow = getItem(position);

        return showFollowing ? follow.getObject(Thing.TARGET) : follow.getObject(Thing.SOURCE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;

        if (convertView != null) {
            view = convertView;
        }
        else {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.person_list_item, parent, false);
        }

        DynamicRealmObject person = getPerson(position);

        if(person != null) {
            Picasso.with(context)
                    .load(Functions.getImageUrlForSize(person, (int) Util.px(64)))
                    .placeholder(R.color.spacer)
                    .into((ImageView) view.findViewById(R.id.profile));

            ((TextView) view.findViewById(R.id.proximity)).setText(Util.getProximityText(person));
            ((TextView) view.findViewById(R.id.person)).setText(Functions.getFullName(person));
        }

        return view;
    }
}
