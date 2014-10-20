package com.queatz.snappy;

/**
 * Created by jacob on 9/27/14.
 */

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class GCMIntentService extends IntentService {

    public GCMIntentService() {
        super("1098230558363");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("SNAPPY", "" + intent);
        Log.d("SNAPPY", "" + intent.getExtras());
    }
}