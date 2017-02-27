package com.queatz.snappy.activity;

import android.app.Activity;
import android.view.View;

/**
 * Created by jacob on 2/25/17.
 */

class FullscreenActivity extends Activity implements View.OnSystemUiVisibilityChangeListener {

//    private static final long GRACE_DELAY = 2000; // Toast.LENGTH_SHORT
//
//    Runnable onSystemUiVisibilityChangeListener = new Runnable() {
//        @Override
//        public void run() {
//            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
//        }
//    };

    @Override
    protected void onResume() {
        super.onResume();
//        onSystemUiVisibilityChangeListener.run();
//        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(this);
    }


    @Override
    public void onSystemUiVisibilityChange(int i) {
//        getWindow().getDecorView().removeCallbacks(onSystemUiVisibilityChangeListener);
//        getWindow().getDecorView().postDelayed(onSystemUiVisibilityChangeListener, GRACE_DELAY);
    }
}
