package com.queatz.snappy.activity;

import android.app.Activity;

import com.queatz.branch.Branch;
import com.queatz.branch.Branchable;
import com.queatz.snappy.MainApplication;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.team.contexts.TeamContext;

/**
 * Created by jacob on 4/2/17.
 */

public class TeamActivity extends Activity implements TeamContext, Branchable<TeamContext> {
    @Override
    public void to(Branch<TeamContext> branch) {
        Branch.from((TeamContext) this).to(branch);
    }

    @Override
    public Team getTeam() {
        return ((MainApplication) getApplication()).team;
    }
}
