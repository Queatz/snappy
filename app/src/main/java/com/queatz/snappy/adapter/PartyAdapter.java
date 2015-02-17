package com.queatz.snappy.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.queatz.snappy.MainApplication;
import com.queatz.snappy.R;
import com.queatz.snappy.Util;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.things.Party;
import com.queatz.snappy.things.Person;

import java.util.Date;
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

            ImageView awho = (ImageView) whosin.getChildAt(0);
            ViewGroup.LayoutParams lps = awho.getLayoutParams();

            whosin.removeAllViews();

            for (int i = 0; i < incount; i++) {
                awho = new ImageView(context);
                awho.setLayoutParams(lps);
                awho.setImageResource(R.drawable.profile);
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
            action.setText(context.getText(R.string.mark_party_full));
            action.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    team.action.joinParty(null);
                }
            });
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

        view.findViewById(R.id.action_requested).setVisibility(position < 1 ? View.VISIBLE : View.GONE);
        view.findViewById(R.id.updates).setVisibility(position < 2 ? View.VISIBLE : View.GONE);

        ((EditText) view.findViewById(R.id.write_message)).setText("");

        return view;
    }
}
