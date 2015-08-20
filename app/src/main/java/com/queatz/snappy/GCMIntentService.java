package com.queatz.snappy;

/**
 * Created by jacob on 9/27/14.
 */

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONException;
import org.json.JSONObject;

public class GCMIntentService extends IntentService {

    public GCMIntentService() {
        super("GCMIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(intent);

        if (extras != null && !extras.isEmpty()) {
            if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                handle(extras.getString("message"));
            }
        }

        GCMBroadcastReceiver.completeWakefulIntent(intent);
    }

    protected void handle(final String message) {
        if(message == null)
            return;

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                try {
                    ((MainApplication) getApplication()).team.push.got(new JSONObject(message));
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}