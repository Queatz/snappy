package com.queatz.snappy.adapter;

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
import com.queatz.branch.Branch;
import com.queatz.branch.Branchable;
import com.queatz.snappy.MainApplication;
import com.queatz.snappy.R;
import com.queatz.snappy.Util;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.team.Thing;
import com.queatz.snappy.team.actions.OpenProfileAction;
import com.queatz.snappy.team.contexts.ActivityContext;
import com.queatz.snappy.util.Functions;
import com.queatz.snappy.util.TimeUtil;
import com.squareup.picasso.Picasso;

import io.realm.DynamicRealmObject;
import io.realm.RealmBaseAdapter;
import io.realm.RealmResults;

/**
 * Created by jacob on 2/8/15.
 */
public class PartyAdapter extends RealmBaseAdapter<DynamicRealmObject> implements Branchable<ActivityContext> {
    private final Context context;

    public PartyAdapter(Context context, RealmResults<DynamicRealmObject> realmResults) {
        super(realmResults);
        this.context = context;
    }

    @Override
    public void to(Branch<ActivityContext> branch) {
        Branch.from((ActivityContext) context).to(branch);
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

        final DynamicRealmObject party = getItem(position);
        final DynamicRealmObject host = party.getObject(Thing.HOST);

        ImageView profile = ((ImageView) view.findViewById(R.id.profile));

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                to(new OpenProfileAction(host));
            }
        });

        ((TextView) view.findViewById(R.id.name)).setText(party.getString(Thing.NAME));

        if(host != null) {
            String name = String.format(context.getString(R.string.by), Functions.getFullName(host));
            ((TextView) view.findViewById(R.id.by_text)).setText(name);
            Picasso.with(context).load(Functions.getImageUrlForSize(host, (int) Util.px(64)))
                    .placeholder(R.color.spacer)
                    .into(profile);
        }

        ImageView timeIcon = ((ImageView) view.findViewById(R.id.time_icon));

        timeIcon.setImageResource(TimeUtil.isDaytime(party.getDate(Thing.DATE)) ? R.drawable.day : R.drawable.night);

        view.findViewById(R.id.time_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                team.action.openDate((Activity) context, party);
            }
        });

        if (party.hasField(Thing.LOCATION)) {
            final DynamicRealmObject location = party.getObject(Thing.LOCATION);

            view.findViewById(R.id.location_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    team.action.openLocation((Activity) context, location);
                }
            });

            view.findViewById(R.id.location_button).setTag(location);
            ((Activity) context).registerForContextMenu(view.findViewById(R.id.location_button));

            String photoUrl = Util.locationPhoto(location, (int) Util.px(128));

            ImageView locationIcon = (ImageView) view.findViewById(R.id.location_icon);
            Picasso.with(context).load(photoUrl).placeholder(R.drawable.location).into(locationIcon);

            ((TextView) view.findViewById(R.id.location_text)).setText(location == null ? context.getString(R.string.hidden) : location.getString(Thing.NAME));
            ((TextView) view.findViewById(R.id.time_text)).setText(party.getDate(Thing.DATE) == null ? context.getString(R.string.hidden) : TimeUtil.cuteDate(party.getDate(Thing.DATE)));
        }

        String details = party.getString(Thing.ABOUT);

        if(details.isEmpty())
            view.findViewById(R.id.details).setVisibility(View.GONE);
        else {
            view.findViewById(R.id.details).setVisibility(View.VISIBLE);
            ((TextView) view.findViewById(R.id.details)).setText(details);
            ((TextView) view.findViewById(R.id.details)).setGravity(details.length() < 64 ? Gravity.CENTER_HORIZONTAL : Gravity.START);
        }

        RealmResults<DynamicRealmObject> in = team.realm.where("Thing")
                .equalTo(Thing.KIND, "join")
                .equalTo("target.id", party.getString(Thing.ID))
                .equalTo("status", Config.JOIN_STATUS_IN).findAll();

        if(in.size() < 1)
            view.findViewById(R.id.whos_in_layout).setVisibility(View.GONE);
        else {
            view.findViewById(R.id.whos_in_layout).setVisibility(View.VISIBLE);

            LinearLayout whosin = ((LinearLayout) view.findViewById(R.id.whos_in_layout));

            whosin.removeAllViews();

            for (DynamicRealmObject j : in) {
                final DynamicRealmObject member = j.getObject(Thing.SOURCE);
                FrameLayout memberProfile = (FrameLayout) View.inflate(context, R.layout.party_member, null);
                whosin.addView(memberProfile);
                Picasso.with(context).load(member == null ? "" :
                        Functions.getImageUrlForSize(j.getObject(Thing.SOURCE), (int) Util.px(64)))
                        .placeholder(R.color.spacer)
                        .into((RoundedImageView) memberProfile.findViewById(R.id.profile));

                memberProfile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        to(new OpenProfileAction(member));
                    }
                });
            }
        }

        String userId = team.auth.getUser();

        TextView action = ((TextView) view.findViewById(R.id.action_join));

        view.findViewById(R.id.layout).setBackground(null);

        if(userId != null && party.getObject(Thing.HOST) != null && userId.equals(party.getObject(Thing.HOST).getString(Thing.ID))) {
            if(party.getBoolean(Thing.FULL)) {
                action.setVisibility(View.GONE);
            }
            else {
                action.setVisibility(View.VISIBLE);
                action.setText(context.getText(party.getList(Thing.MEMBERS).size() > 0 ? R.string.mark_party_full : R.string.close_party));
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
            DynamicRealmObject requested = null;

            if(team.auth.getUser() != null) {
                requested = team.realm.where("Thing")
                        .equalTo(Thing.KIND, "join")
                        .equalTo("target.id", party.getString(Thing.ID))
                        .equalTo("source.id", team.auth.getUser()).findFirst();
            }

            if(requested == null || (!party.getBoolean(Thing.FULL) && Config.JOIN_STATUS_WITHDRAWN.equals(requested.getString(Thing.STATUS)))) {
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
                if(Config.JOIN_STATUS_REQUESTED.equals(requested.getString(Thing.STATUS)) ||
                        Config.JOIN_STATUS_OUT.equals(requested.getString(Thing.STATUS))) {
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

            if(requested != null && Config.JOIN_STATUS_IN.equals(requested.getString(Thing.STATUS))) {
                view.findViewById(R.id.layout).setBackgroundResource(R.drawable.youre_in);
            }
        }

        ListView actions = (ListView) view.findViewById(R.id.actions);

        RealmResults<DynamicRealmObject> joinRequests = null;

        if(team.auth.getUser() != null && party.getObject(Thing.HOST) != null && team.auth.getUser().equals(party.getObject(Thing.HOST).getString(Thing.ID))) {
            joinRequests = team.realm.where("Thing")
                    .equalTo(Thing.KIND, "join")
                    .equalTo("target.id", party.getString(Thing.ID))
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
