package com.queatz.snappy.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.queatz.snappy.MainApplication;
import com.queatz.snappy.R;
import com.queatz.snappy.adapter.QuestAdapter;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.things.Quest;
import com.queatz.snappy.ui.TimeSlider;

import java.util.Date;

import io.realm.RealmResults;

/**
 * Created by jacob on 9/16/15.
 */
public class Quests extends Activity {
    Team team;
    Object mContextObject;
    EditText questDetails;
    EditText questName;
    EditText questReward;
    SwipeRefreshLayout mRefresh;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        team = ((MainApplication) getApplication()).team;

        setContentView(R.layout.quests);

        final ListView questList = (ListView) findViewById(R.id.quests);

        if(Config.HOSTING_ENABLED_TRUE.equals(team.buy.hostingEnabled())) {
            View newQuest = View.inflate(this, R.layout.quests_new, null);

            questList.addHeaderView(newQuest);
            questList.addFooterView(new View(this));

            questDetails = (EditText) newQuest.findViewById(R.id.details);
            questName = (EditText) newQuest.findViewById(R.id.name);
            questReward = (EditText) newQuest.findViewById(R.id.reward);
            final TimeSlider timeOfDaySlider = (TimeSlider) newQuest.findViewById(R.id.time);
            final TimeSlider teamSizeSlider = (TimeSlider) newQuest.findViewById(R.id.teamSize);
            final Button newButton = (Button) newQuest.findViewById(R.id.start);

            timeOfDaySlider.setPercent(0);

            timeOfDaySlider.setTextCallback(new TimeSlider.TextCallback() {
                @Override
                public String getText(float percent) {
                    return getTimeOfDay(percent);
                }
            });

            teamSizeSlider.setPercent(0);

            teamSizeSlider.setTextCallback(new TimeSlider.TextCallback() {
                @Override
                public String getText(float percent) {
                    int people = getTeamSize(percent);
                    return getResources().getQuantityString(R.plurals.people, people, people);
                }
            });

            newButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (team.action.newQuest(
                            questName.getText().toString(),
                            questDetails.getText().toString(),
                            questReward.getText().toString(),
                            getTimeOfDay(timeOfDaySlider.getPercent()),
                            getTeamSize(teamSizeSlider.getPercent())
                    )) {
                        timeOfDaySlider.setPercent(0);
                        teamSizeSlider.setPercent(0);
                        questDetails.setText("");
                        questReward.setText("");
                        questName.setText("");
                    }
                }
            });
        }

        RealmResults<Quest> query = team.realm.where(Quest.class).greaterThan("opened", new Date(new Date().getTime() - 1000L * 60 * 60 * 24 * 30))
                .notEqualTo("status", Config.QUEST_STATUS_COMPLETE)
                .beginGroup()
                    .equalTo("status", Config.QUEST_STATUS_OPEN)
                    .or()
                    .equalTo("team.id", team.auth.getUser())
                    .or()
                    .equalTo("host.id", team.auth.getUser())
                .endGroup()
                .findAllSorted("opened", false);

        final QuestAdapter adapter = new QuestAdapter(this, query);
        questList.setAdapter(adapter);

        mRefresh = (SwipeRefreshLayout) findViewById(R.id.refresh);
        mRefresh.setColorSchemeResources(R.color.purple);
        mRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });
    }

    private void refresh() {
        team.here.update(this, mRefresh, null);
    }

    static String[] timesOfDay = {
            "Anytime",
            "Early Morning",
            "Morning",
            "Day",
            "Evening",
            "Late Evening",
            "Night"
    };

    private String getTimeOfDay(float percent) {
        return timesOfDay[Math.min(timesOfDay.length - 1, (int) (percent * timesOfDay.length))];
    }

    private int getTeamSize(float percent) {
        return (int) Math.min(6, 1 + percent * 6);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        team.onActivityResult(this, requestCode, resultCode, data);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        mContextObject = view.getTag();
        team.menu.make(mContextObject, menu, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        return team.menu.choose(this, mContextObject, item);
    }

    @Override
    public void onContextMenuClosed(Menu menu) {
        super.onContextMenuClosed(menu);
    }
}
