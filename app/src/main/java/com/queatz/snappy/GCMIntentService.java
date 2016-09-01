package com.queatz.snappy;

/**
 * Created by jacob on 9/27/14.
 */

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.google.android.gms.gcm.GcmListenerService;

public class GCMIntentService extends GcmListenerService {

    @Override
    public void onMessageReceived(String s, Bundle bundle) {
        if (bundle != null && !bundle.isEmpty()) {
            handle(bundle.getString("message"));
        }
    }

    protected void handle(final String message) {
        if(message == null)
            return;

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                ((MainApplication) getApplication()).team.push.got(message);
            }
        });
    }
}