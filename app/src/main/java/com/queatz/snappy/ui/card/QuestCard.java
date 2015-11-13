package com.queatz.snappy.ui.card;

import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.queatz.snappy.MainApplication;
import com.queatz.snappy.R;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.things.Person;
import com.queatz.snappy.things.Quest;
import com.squareup.picasso.Picasso;

import io.realm.RealmObject;

/**
 * Created by jacob on 11/12/15.
 */
public class QuestCard implements Card<Quest> {
    @Override
    public View getCard(final Context context, final Quest quest, View convertView, ViewGroup parent) {
        View view;

        final Team team = ((MainApplication) context.getApplicationContext()).team;

        if (convertView != null) {
            view = convertView;
        }
        else {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.quests_item, parent, false);
        }

        final ViewGroup people = (ViewGroup) view.findViewById(R.id.people);
        final TextView name = (TextView) view.findViewById(R.id.name);
        final TextView details = (TextView) view.findViewById(R.id.details);
        final TextView reward = (TextView) view.findViewById(R.id.reward);
        final TextView time = (TextView) view.findViewById(R.id.time);
        final View actions = view.findViewById(R.id.actions);
        final Button action = (Button) view.findViewById(R.id.action_start);

        name.setText(quest.getName());
        time.setText(quest.getTime());
        reward.setText(Html.fromHtml(context.getString(R.string.reward_text, quest.getReward())));

        if(team.auth.getUser() != null && !team.auth.getUser().equals(quest.getHost().getId())) {
            view.setTag(quest);
            ((Activity) context).registerForContextMenu(view);
        }

        // Details
        details.setText(Html.fromHtml(context.getString(R.string.details_text, quest.getDetails())));

        // Action

        boolean alreadyStarted = false;

        for (Person person : quest.getTeam()) {
            if (team.auth.getUser().equals(person.getId())) {
                alreadyStarted = true;
                break;
            }
        }

        if (team.auth.getUser().equals(quest.getHost().getId())) {
            alreadyStarted = true;
            action.setText(context.getString(quest.getTeam().isEmpty() ? R.string.close_quest : R.string.mark_complete));
            action.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    team.action.markQuestComplete(quest);
                }
            });
        } else if (alreadyStarted) {
            action.setText(context.getString(R.string.message));
            action.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    team.action.openMessages((Activity) context, quest.getHost());
                }
            });
        } else {
            action.setText(context.getString(quest.getTeam().size() == quest.getTeamSize() -1 ? R.string.start_quest : R.string.join_quest));
            action.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    team.action.startQuest((Activity) context, quest);
                }
            });
        }

        if (alreadyStarted) {
            view.findViewById(R.id.content).setBackgroundResource(R.drawable.youre_in_quest);
        } else {
            view.findViewById(R.id.content).setBackground(null);
        }

        // Team

        people.removeAllViews();

        for (final Person person : quest.getTeam()) {
            View.inflate(context, R.layout.quests_item_person, people);
            ImageView imageView = (ImageView) people.getChildAt(people.getChildCount() - 1);

            Picasso.with(context)
                    .load(person.getImageUrlForSize(imageView.getMeasuredWidth()))
                    .into(imageView);

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    team.action.openProfile((Activity) context, person);
                }
            });
        }

        for (int i = quest.getTeam().size(); i < quest.getTeamSize(); i++) {
            View.inflate(context, R.layout.quests_item_person, people);
            ImageView imageView = (ImageView) people.getChildAt(people.getChildCount() - 1);

            imageView.setImageResource(R.color.darkpurple);
        }

        return view;
    }
}
