package com.queatz.snappy.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.queatz.snappy.MainApplication;
import com.queatz.snappy.R;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.things.Bounty;
import com.queatz.snappy.ui.RevealAnimation;
import com.squareup.picasso.Picasso;

import io.realm.RealmBaseAdapter;
import io.realm.RealmResults;

/**
 * Created by jacob on 8/29/15.
 */
public class BountyAdapter extends RealmBaseAdapter<Bounty> {
    public BountyAdapter(Context context, RealmResults<Bounty> realmResults) {
        super(context, realmResults, true);
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
            view = inflater.inflate(R.layout.bounties_item, parent, false);
        }

        final Bounty bounty = realmResults.get(position);

        ImageView people = (ImageView) view.findViewById(R.id.people);
        ImageView profile = (ImageView) view.findViewById(R.id.profile);
        TextView details = (TextView) view.findViewById(R.id.details);
        TextView price = (TextView) view.findViewById(R.id.price);

        Picasso.with(context)
                .load(bounty.getPoster().getImageUrlForSize(profile.getMeasuredWidth()))
                .into(profile);

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                team.action.openProfile((Activity) context, bounty.getPoster());
            }
        });

        details.setText(bounty.getDetails());
        price.setText(bounty.getPrice() > 0 ? "$" + bounty.getPrice() : context.getString(R.string.free));

        if(team.auth.getUser() != null && team.auth.getUser().equals(bounty.getPoster().getId())) {
            view.setTag(bounty);
            ((Activity) context).registerForContextMenu(view);
        }

        if(bounty.getPeople().size() > 0) {
            Picasso.with(context)
                    .load(bounty.getPeople().first().getImageUrlForSize(people.getMeasuredWidth()))
                    .into(people);

            people.setVisibility(View.VISIBLE);

            people.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    team.action.openProfile((Activity) context, bounty.getPeople().first());
                }
            });
        }
        else {
            people.setVisibility(View.GONE);
        }

        return view;
    }
}
