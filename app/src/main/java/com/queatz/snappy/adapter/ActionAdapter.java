package com.queatz.snappy.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.makeramen.RoundedImageView;
import com.queatz.snappy.Config;
import com.queatz.snappy.MainApplication;
import com.queatz.snappy.R;
import com.queatz.snappy.Util;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.things.Join;
import com.queatz.snappy.things.Party;
import com.queatz.snappy.things.Person;
import com.squareup.picasso.Picasso;

import java.util.Random;

import io.realm.RealmBaseAdapter;
import io.realm.RealmResults;

/**
 * Created by jacob on 2/18/15.
 */
public class ActionAdapter extends RealmBaseAdapter<Join> {
    public ActionAdapter(Context context, RealmResults<Join> realmResults) {
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
            view = inflater.inflate(R.layout.action, parent, false);
        }

        final Join join = realmResults.get(position);
        final Person person = join.getPerson();
        final String status = join.getStatus();

        ImageView profile = ((ImageView) view.findViewById(R.id.profile));
        Picasso.with(context).load(person == null ? "" : person.getImageUrlForSize((int) Util.px(context, 64))).placeholder(R.color.spacer).into(profile);

        if(person == null || !Config.JOIN_STATUS_REQUESTED.equals(status)) {
            ((TextView) view.findViewById(R.id.action)).setText("-");
        }
        else {
            ((TextView) view.findViewById(R.id.action)).setText(String.format(context.getString(R.string.accept_person), person.getName()));
        }

        final Team team = ((MainApplication) context.getApplicationContext()).team;

        return view;
    }
}
