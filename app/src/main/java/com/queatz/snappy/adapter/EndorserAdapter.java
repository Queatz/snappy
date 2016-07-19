package com.queatz.snappy.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.queatz.snappy.R;
import com.queatz.snappy.Util;
import com.queatz.snappy.things.Endorsement;
import com.queatz.snappy.things.Person;
import com.squareup.picasso.Picasso;

import io.realm.RealmBaseAdapter;
import io.realm.RealmResults;

/**
 * Created by jacob on 12/26/15.
 */
public class EndorserAdapter extends RealmBaseAdapter<Endorsement> {
    public EndorserAdapter(Context context, RealmResults<Endorsement> realmResults) {
        super(context, realmResults, true);
    }

    public Person getPerson(int position) {
        Endorsement endorsement = realmResults.get(position);

        return endorsement.getSource();
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

            ((TextView) view.findViewById(R.id.person)).setText(person.getName());
        }

        return view;
    }
}
