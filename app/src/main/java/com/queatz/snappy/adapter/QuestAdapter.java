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
import com.queatz.snappy.things.Quest;
import com.squareup.picasso.Picasso;

import io.realm.RealmBaseAdapter;
import io.realm.RealmResults;

/**
 * Created by jacob on 9/16/15.
 */
public class QuestAdapter extends RealmBaseAdapter<Quest> {
    public QuestAdapter(Context context, RealmResults<Quest> realmResults) {
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
            view = inflater.inflate(R.layout.quests_item, parent, false);
        }

        final Quest quest = realmResults.get(position);

        ImageView people = (ImageView) view.findViewById(R.id.people);
        TextView details = (TextView) view.findViewById(R.id.details);
        TextView reward = (TextView) view.findViewById(R.id.reward);

        details.setText(quest.getDetails());
        reward.setText(quest.getReward());

        if(team.auth.getUser() != null && team.auth.getUser().equals(quest.getHost().getId())) {
            view.setTag(quest);
            ((Activity) context).registerForContextMenu(view);
        }

        // CHeck if you are in it and don't show start

        if(quest.getTeam().size() > 0) {
            Picasso.with(context)
                    .load(quest.getTeam().first().getImageUrlForSize(people.getMeasuredWidth()))
                    .into(people);

            people.setVisibility(View.VISIBLE);

            people.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    team.action.openProfile((Activity) context, quest.getTeam().first());
                }
            });
        }
        else {
            people.setVisibility(View.GONE);
        }

        return view;
    }
}
