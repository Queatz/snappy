package com.queatz.snappy;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.queatz.snappy.shared.Config;
import com.queatz.snappy.team.Team;

/**
 * Created by jacob on 9/18/17.
 */

public class WallpaperChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Team team = ((MainApplication) context.getApplicationContext()).team;
        team.callbacks.onActivityResult(null, Config.REQUEST_CODE_WALLPAPER_CHANGED, Activity.RESULT_OK, intent);
    }
}
