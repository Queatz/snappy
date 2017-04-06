package com.queatz.snappy.activity;

import android.app.Activity;

import com.queatz.branch.Branch;
import com.queatz.branch.Branchable;
import com.queatz.snappy.Util;
import com.queatz.snappy.team.TeamActivity;
import com.queatz.snappy.team.contexts.ActivityContext;

/**
 * Created by jacob on 2/25/17.
 */

class FullscreenActivity extends TeamActivity implements Branchable<ActivityContext> {

    @Override
    public void to(Branch<ActivityContext> branch) {
        Branch.from((ActivityContext) this).to(branch);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Util.autoHideWindowUI(getWindow());
    }
}
