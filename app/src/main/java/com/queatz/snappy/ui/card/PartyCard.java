package com.queatz.snappy.ui.card;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.makeramen.RoundedImageView;
import com.queatz.snappy.MainApplication;
import com.queatz.snappy.R;
import com.queatz.snappy.Util;
import com.queatz.snappy.adapter.ActionAdapter;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.things.Join;
import com.queatz.snappy.things.Party;
import com.queatz.snappy.things.Person;
import com.queatz.snappy.util.TimeUtil;
import com.squareup.picasso.Picasso;

import io.realm.RealmResults;

/**
 * Created by jacob on 11/12/15.
 */
public class PartyCard implements Card<Party> {
    public View getCard(final Context context, final Party party, View convertView, ViewGroup parent) {
        View view;

        if (convertView != null) {
            view = convertView;
        }
        else {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.party_card, parent, false);
        }

        view.setTag(party);

        final Team team = ((MainApplication) context.getApplicationContext()).team;

        final Person host = party.getHost();

        ImageView profile = ((ImageView) view.findViewById(R.id.profile));

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
            Picasso.with(context).load(host.getImageUrlForSize((int) Util.px(64))).placeholder(R.color.spacer).into(profile);
        }

        ImageView timeIcon = ((ImageView) view.findViewById(R.id.time_icon));

        timeIcon.setImageResource(TimeUtil.isDaytime(party.getDate()) ? R.drawable.day : R.drawable.night);

        view.findViewById(R.id.time_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                team.action.openDate((Activity) context, party);
            }
        });

        if (party.getLocation() != null) {
            view.findViewById(R.id.location_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    team.action.openLocation((Activity) context, party.getLocation());
                }
            });

            view.findViewById(R.id.location_button).setTag(party.getLocation());
            ((Activity) context).registerForContextMenu(view.findViewById(R.id.location_button));

            String photoUrl = Util.locationPhoto(party.getLocation(), (int) Util.px(128));

            ImageView locationIcon = (ImageView) view.findViewById(R.id.location_icon);
            ImageView backdrop = ((ImageView) view.findViewById(R.id.backdrop));
            Picasso.with(context).load(photoUrl).placeholder(R.drawable.location).into(locationIcon);
            Picasso.with(context).load(photoUrl).placeholder(R.drawable.location).into(backdrop);

            ((TextView) view.findViewById(R.id.location_text)).setText(party.getLocation() == null ? context.getString(R.string.hidden) : party.getLocation().getName());
            ((TextView) view.findViewById(R.id.time_text)).setText(party.getDate() == null ? context.getString(R.string.hidden) : TimeUtil.cuteDate(party.getDate()));
        }

        String details = party.getDetails();

        if(details.isEmpty())
            view.findViewById(R.id.details).setVisibility(View.GONE);
        else {
            view.findViewById(R.id.details).setVisibility(View.VISIBLE);
            ((TextView) view.findViewById(R.id.details)).setText(details);
            ((TextView) view.findViewById(R.id.details)).setGravity(details.length() < 64 ? Gravity.CENTER_HORIZONTAL : Gravity.START);
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

        String userId = team.auth.getUser();

        TextView action = ((TextView) view.findViewById(R.id.action_join));

        view.findViewById(R.id.layout).setBackground(null);

        if(userId != null && party.getHost() != null && userId.equals(party.getHost().getId())) {
            if(party.isFull()) {
                action.setVisibility(View.GONE);
            }
            else {
                action.setVisibility(View.VISIBLE);
                action.setText(context.getText(party.getPeople().size() > 0 ? R.string.mark_party_full : R.string.close_party));
                action.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        team.action.markPartyFull(party);
                    }
                });
            }

            view.findViewById(R.id.layout).setBackgroundResource(R.drawable.youre_in);
        }
        else {
            Join requested = null;

            if(team.auth.getUser() != null) {
                requested = team.realm.where(Join.class)
                        .equalTo("party.id", party.getId())
                        .equalTo("person.id", team.auth.getUser()).findFirst();
            }

            if(requested == null || (!party.isFull() && Config.JOIN_STATUS_WITHDRAWN.equals(requested.getStatus()))) {
                action.setVisibility(View.VISIBLE);
                action.setText(context.getText(R.string.interested));
                action.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        team.action.joinParty((Activity) context, party);
                    }
                });
            }
            else {
                if(Config.JOIN_STATUS_REQUESTED.equals(requested.getStatus()) ||
                        Config.JOIN_STATUS_OUT.equals(requested.getStatus())) {
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

            if(requested != null && Config.JOIN_STATUS_IN.equals(requested.getStatus())) {
                view.findViewById(R.id.layout).setBackgroundResource(R.drawable.youre_in);
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

        return view;
    }
}