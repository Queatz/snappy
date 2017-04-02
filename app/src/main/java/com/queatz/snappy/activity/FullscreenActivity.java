package com.queatz.snappy.activity;

import android.app.Activity;

import com.queatz.branch.Branch;
import com.queatz.branch.Branchable;
import com.queatz.snappy.Util;

/**
 * Created by jacob on 2/25/17.
 */

class FullscreenActivity extends Activity implements Branchable<Activity> {

    @Override
    public void to(Branch<Activity> branch) {
        Branch.from((Activity) this).to(branch);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Util.autoHideWindowUI(getWindow());
    }
}
