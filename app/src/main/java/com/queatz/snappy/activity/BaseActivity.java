package com.queatz.snappy.activity;

import android.app.Activity;

/**
 * Created by jacob on 2/20/15.
 */
public class BaseActivity extends Activity {
    @Override
    public void finish() {
        super.finish();
        //overridePendingTransition(android.R.anim.fade_in, android.R.anim.slide_out_right);
    }
}
