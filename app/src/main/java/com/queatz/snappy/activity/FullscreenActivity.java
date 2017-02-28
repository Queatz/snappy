package com.queatz.snappy.activity;

import android.app.Activity;

import com.queatz.snappy.Util;

/**
 * Created by jacob on 2/25/17.
 */

class FullscreenActivity extends Activity {

    @Override
    protected void onResume() {
        super.onResume();
        Util.autoHideWindowUI(getWindow());
    }
}
