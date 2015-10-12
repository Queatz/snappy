package com.queatz.snappy.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.queatz.snappy.R;
import com.queatz.snappy.Util;
import com.queatz.snappy.things.Follow;
import com.queatz.snappy.things.Person;
import com.squareup.picasso.Picasso;

import io.realm.RealmBaseAdapter;
import io.realm.RealmResults;

/**
 * Created by jacob on 2/19/15.
 */
public class PersonListAdapter extends RealmBaseAdapter<Follow> {
    boolean showFollowing;

    public PersonListAdapter(Context context, RealmResults<Follow> realmResults, boolean showFollowing) {
        super(context, realmResults, true);
        this.showFollowing = showFollowing;
    }

    public Person getPerson(int position) {
        Follow follow = realmResults.get(position);

        return showFollowing ? follow.getFollowing() : follow.getPerson();
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

        Person person = getPerson(position);

        if(person != null) {
            Picasso.with(context)
                    .load(person.getImageUrlForSize((int) Util.px(64)))
                    .placeholder(R.color.spacer)
                    .into((ImageView) view.findViewById(R.id.profile));

            ((TextView) view.findViewById(R.id.person)).setText(Util.fancyName(person));
        }

        return view;
    }
}
