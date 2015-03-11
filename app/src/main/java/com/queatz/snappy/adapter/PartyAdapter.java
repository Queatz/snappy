package com.queatz.snappy.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
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

        final Team team = ((MainApplication) context.getApplicationContext()).team;

        final Party party = realmResults.get(position);
        final Person host = party.getHost();

        ImageView profile = ((ImageView) view.findViewById(R.id.profile));
        Picasso.with(context).load(host == null ? "" : host.getImageUrlForSize((int) Util.px(64))).placeholder(R.color.spacer).into(profile);

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(host != null)
                    team.action.openProfile((Activity) context, host);
            }
        });

        ((TextView) view.findViewById(R.id.name)).setText(party.getName());

        if(host != null) {
            String name = String.format(context.getString(R.string.by), host.getFirstName() + " " + host.getLastName());
            ((TextView) view.findViewById(R.id.by_text)).setText(name);
        }

        ImageView timeIcon = ((ImageView) view.findViewById(R.id.time_icon));

        timeIcon.setImageResource(Util.isDaytime(party.getDate()) ? R.drawable.day : R.drawable.night);

        view.findViewById(R.id.time_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                team.action.openDate((Activity) context, party);
            }
        });

        view.findViewById(R.id.location_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                team.action.openLocation((Activity) context, party.getLocation());
            }
        });

        ((TextView) view.findViewById(R.id.location_text)).setText(party.getLocation() == null ? context.getString(R.string.hidden) : party.getLocation().getName());
        ((TextView) view.findViewById(R.id.time_text)).setText(party.getDate() == null ? context.getString(R.string.hidden) : Util.cuteDate(party.getDate()));

        String details = party.getDetails();

        if(details.isEmpty())
            view.findViewById(R.id.details).setVisibility(View.GONE);
        else {
            view.findViewById(R.id.details).setVisibility(View.VISIBLE);
            ((TextView) view.findViewById(R.id.details)).setText(details);
        }

        RealmResults<Join> in = team.realm.where(Join.class)
                .equalTo("party.id", party.getId())
                .equalTo("status", Config.JOIN_STATUS_IN).findAll();

        if(in.size() < 1)
            view.findViewById(R.id.whos_in_layout).setVisibility(View.GONE);
        else {
            view.findViewById(R.id.whos_in_layout).setVisibility(View.VISIBLE);

            LinearLayout whosin = ((LinearLayout) view.findViewById(R.id.whos_in_layout));

            whosin.removeAllViews();

            for (Join j : in) {
                final Person member = j.getPerson();
                FrameLayout memberProfile = (FrameLayout) View.inflate(context, R.layout.party_member, null);
                whosin.addView(memberProfile);
                Picasso.with(context).load(member == null ? "" : j.getPerson().getImageUrlForSize((int) Util.px(64))).placeholder(R.color.spacer).into((RoundedImageView) memberProfile.findViewById(R.id.profile));

                memberProfile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (member != null)
                            team.action.openProfile((Activity) context, member);
                    }
                });
            }
        }

        ((ImageView) view.findViewById(R.id.backdrop)).setImageResource(R.drawable.location);

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
            Join requested = team.realm.where(Join.class)
                    .equalTo("party.id", party.getId())
                    .equalTo("person.id", team.auth.getUser()).findFirst();

            if (requested != null) {
                if(Config.JOIN_STATUS_REQUESTED.equals(requested.getStatus())) {
                    action.setVisibility(View.VISIBLE);

                    action.setText(context.getText(R.string.requested));
                    action.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new AlertDialog.Builder(context).setItems(R.array.requested_menu, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String items[] = context.getResources().getStringArray(R.array.requested_menu);

                                    if(context.getString(R.string.cancel).equals(items[which])) {
                                        team.action.cancelJoin(party);
                                    }
                                }
                            }).show();
                        }
                    });
                }
                else {
                    action.setVisibility(View.GONE);
                }
            }
            else  {
                action.setVisibility(View.VISIBLE);
                action.setText(context.getText(R.string.request_to_join));
                action.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        team.action.joinParty(party);
                    }
                });
            }
        }

        ListView actions = (ListView) view.findViewById(R.id.actions);

        RealmResults<Join> joinRequests = null;

        if(team.auth.getUser() != null && party.getHost() != null && team.auth.getUser().equals(party.getHost().getId())) {
            joinRequests = team.realm.where(Join.class)
                    .equalTo("party.id", party.getId())
                    .equalTo("status", Config.JOIN_STATUS_REQUESTED).findAll();
        }

        if(joinRequests != null && joinRequests.size() > 0) {
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
