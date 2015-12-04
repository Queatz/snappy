package com.queatz.snappy;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.queatz.snappy.team.Team;

/**
 * Created by jacob on 12/3/15.
 */
public class AdvertiseBroadcastReciever extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Team team = ((MainApplication) context.getApplicationContext()).team;

        team.advertise.hidePerson(intent.getStringExtra("deviceAddress"));

        setResultCode(Activity.RESULT_OK);
    }
}
