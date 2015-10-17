package com.queatz.snappy.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.queatz.snappy.shared.Config;
import com.queatz.snappy.MainApplication;
import com.queatz.snappy.R;
import com.queatz.snappy.Util;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.things.Join;
import com.queatz.snappy.things.Person;
import com.squareup.picasso.Picasso;

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

        final Team team = ((MainApplication) context.getApplicationContext()).team;

        final Join join = realmResults.get(position);
        final Person person = join.getPerson();
        final String status = join.getStatus();

        ImageView profile = ((ImageView) view.findViewById(R.id.profile));
        Picasso.with(context).load(person == null ? "" : person.getImageUrlForSize((int) Util.px(64))).placeholder(R.color.spacer).into(profile);

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                team.action.openProfile((Activity) context, person);
            }
        });

        if(person == null || !Config.JOIN_STATUS_REQUESTED.equals(status)) {
            ((TextView) view.findViewById(R.id.action)).setText("-");
        }
        else {
            ((TextView) view.findViewById(R.id.action)).setText(Html.fromHtml(String.format(context.getString(R.string.accept_person), person.getName())));
        }

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Team team = ((MainApplication) context.getApplicationContext()).team;

                team.action.acceptJoin(join);
            }
        });

        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final Team team = ((MainApplication) context.getApplicationContext()).team;

                new AlertDialog.Builder(context).setItems(R.array.join_request_menu, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String items[] = context.getResources().getStringArray(R.array.join_request_menu);

                        if(context.getString(R.string.hide).equals(items[which])) {
                            team.action.hideJoin(join);
                        }
                    }
                }).show();

                return true;
            }
        });

        return view;
    }
}
