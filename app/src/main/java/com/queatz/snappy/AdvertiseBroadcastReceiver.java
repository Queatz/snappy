package com.queatz.snappy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.queatz.snappy.shared.Config;
import com.queatz.snappy.team.Team;

/**
 * Created by jacob on 12/3/15.
 */
public class AdvertiseBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Team team = ((MainApplication) context.getApplicationContext()).team;

        Intent serviceIntent = new Intent(context, AdvertiseService.class);

        if (intent != null) {
            Bundle extras = new Bundle();
            extras.putString(Config.EXTRA_ACTION, intent.getAction());
            serviceIntent.putExtras(extras);
        }

        context.startService(serviceIntent);

        if (intent != null) {
            if (Config.EXTRA_ACTION_HIDE.equals(intent.getStringExtra(Config.EXTRA_ACTION))) {
                if (intent.hasExtra("deviceAddress")) {
                    team.advertise.hidePerson(intent.getStringExtra("deviceAddress"));
                }
            }
        }
    }
}
