package com.queatz.snappy.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
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
 * Created by jacob on 2/8/15.
 */
public class PartyAdapter extends RealmBaseAdapter<Party> {
    public PartyAdapter(Context context, RealmResults<Party> realmResults) {
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
            view = inflater.inflate(R.layout.party_card, parent, false);
        }

        final Party party = realmResults.get(position);
        final Person host = party.getHost();

        ImageView profile = ((ImageView) view.findViewById(R.id.profile));
        Picasso.with(context).load(host == null ? "" : host.getImageUrlForSize((int) Util.px(context, 64))).placeholder(R.color.spacer).into(profile);

        ((TextView) view.findViewById(R.id.name)).setText(party.getName());

        if(host != null) {
            String name = String.format(context.getString(R.string.by), host.getFirstName() + " " + host.getLastName());
            ((TextView) view.findViewById(R.id.by_text)).setText(name);
        }

        ((TextView) view.findViewById(R.id.location_text)).setText(party.getLocation() == null ? context.getString(R.string.hidden) : party.getLocation().getName());
        ((TextView) view.findViewById(R.id.time_text)).setText(party.getDate() == null ? context.getString(R.string.hidden) : Util.cuteDate(context, party.getDate()));

        String details = party.getDetails();

        if(details.isEmpty())
            view.findViewById(R.id.details).setVisibility(View.GONE);
        else {
            view.findViewById(R.id.details).setVisibility(View.VISIBLE);
            ((TextView) view.findViewById(R.id.details)).setText(details);
        }

        int incount = new Random().nextInt() % 8;

        if(incount < 1)
            view.findViewById(R.id.whos_in_layout).setVisibility(View.GONE);
        else {
            view.findViewById(R.id.whos_in_layout).setVisibility(View.VISIBLE);

            LinearLayout whosin = ((LinearLayout) view.findViewById(R.id.whos_in));

            RoundedImageView awho = (RoundedImageView) whosin.getChildAt(0);
            ViewGroup.LayoutParams lps = awho.getLayoutParams();

            whosin.removeAllViews();

            float radius = awho.getCornerRadius();

            for (int i = 0; i < incount; i++) {
                awho = new RoundedImageView(context);
                awho.setCornerRadius(radius);
                awho.setLayoutParams(lps);
                awho.setImageResource(R.drawable.sherry);
                whosin.addView(awho);
            }
        }

        incount = Math.abs(new Random().nextInt() % 5);

        ((ImageView) view.findViewById(R.id.backdrop)).setImageResource(new int[] {
                R.drawable.backdrop_location,
                R.drawable.backdrop_location_2,
                R.drawable.backdrop_location_3,
                R.drawable.backdrop_location_4,
                R.drawable.backdrop_location_5
        }[incount]);

        final Team team = ((MainApplication) context.getApplicationContext()).team;

        String userId = team.auth.getUser();

        TextView action = ((TextView) view.findViewById(R.id.action_join));

        if(userId != null && party.getHost() != null && userId.equals(party.getHost().getId())) {
            if(party.isFull()) {
                action.setVisibility(View.GONE);
            }
            else {
                action.setVisibility(View.VISIBLE);
                action.setText(context.getText(R.string.mark_party_full));
                action.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        team.action.markPartyFull(party);
                    }
                });
            }
        }
        else {
            action.setText(context.getText(R.string.request_to_join));
            action.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    team.action.joinParty(party);
                }
            });
        }

        ListView actions = (ListView) view.findViewById(R.id.actions);

        RealmResults<Join> joinRequests = team.realm.where(Join.class)
                .equalTo("party.id", party.getId())
                .equalTo("status", Config.JOIN_STATUS_REQUESTED).findAll();

        if(joinRequests.size() > 0) {
            actions.setVisibility(View.VISIBLE);
            actions.setAdapter(new ActionAdapter(context, joinRequests));
        }
        else {
            actions.setVisibility(View.GONE);
        }

        view.findViewById(R.id.updates).setVisibility(View.GONE);

        ((EditText) view.findViewById(R.id.write_message)).setText("");

        return view;
    }
}
