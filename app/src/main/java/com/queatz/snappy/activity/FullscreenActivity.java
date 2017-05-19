package com.queatz.snappy.activity;

import com.queatz.snappy.Util;
import com.queatz.snappy.team.TeamActivity;

/**
 * Created by jacob on 2/25/17.
 */

class FullscreenActivity extends TeamActivity {

    @Override
    protected void onResume() {
        super.onResume();
        Util.autoHideWindowUI(getWindow());
    }
}
