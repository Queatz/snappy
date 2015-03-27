package com.queatz.snappy.team;

import android.app.Activity;
import android.content.Intent;

import com.queatz.snappy.Config;

/**
 * Created by jacob on 3/27/15.
 */
public class Buy {
    public static interface Callback {
        void onSuccess();
        void onError();
    }

    public Team team;

    public Buy(Team t) {
        team = t;
    }

    public void buy(final Activity activity, final Callback callback) {
//        activity.bindService();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Config.REQUEST_CODE_BUY_INTENT:
                if(resultCode == Activity.RESULT_OK) {

                }
                else {

                }
                break;
        }
    }
}
