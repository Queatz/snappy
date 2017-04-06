package com.queatz.snappy.team.actions;

import com.queatz.branch.Branch;
import com.queatz.snappy.MainApplication;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.team.contexts.ActivityContext;
import com.queatz.snappy.team.contexts.TeamContext;

/**
 * Created by jacob on 4/2/17.
 */

public class ActivityAction extends Branch<ActivityContext> implements TeamContext {
    @Override
    public Team getTeam() {
        return ((MainApplication) me().getActivity().getApplicationContext()).team;
    }
}
