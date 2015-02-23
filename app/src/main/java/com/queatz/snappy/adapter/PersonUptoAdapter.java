package com.queatz.snappy.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.queatz.snappy.R;
import com.queatz.snappy.Util;
import com.queatz.snappy.things.Person;
import com.queatz.snappy.things.Update;
import com.squareup.picasso.Picasso;

import io.realm.RealmBaseAdapter;
import io.realm.RealmResults;

/**
 * Created by jacob on 2/18/15.
 */
public class PersonUptoAdapter extends RealmBaseAdapter<Update> {
    public PersonUptoAdapter(Context context, RealmResults<Update> realmResults) {
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
            view = inflater.inflate(R.layout.person_upto_item, parent, false);
        }

        Update update = realmResults.get(position);

        Person person = update.getPerson();

        if(person != null) {

            Picasso.with(context)
                    .load(person.getImageUrlForSize((int) Util.px(64)))
                    .placeholder(R.color.spacer)
                    .into((ImageView) view.findViewById(R.id.profile));
        }

        ((TextView) view.findViewById(R.id.text)).setText(Util.cuteDate(update.getDate()));

        return view;
    }
}
