package com.queatz.snappy.team.actions;

import android.app.Activity;

import com.queatz.branch.Branch;
import com.queatz.snappy.MainApplication;
import com.queatz.snappy.team.Team;

/**
 * Created by jacob on 4/2/17.
 */

public class ActivityAction extends Branch<Activity> {
    public Team getTeam() {
        return ((MainApplication) me().getApplicationContext()).team;
    }

    public Activity getActivity() {
        return me();
    }
}
