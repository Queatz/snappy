package com.queatz.snappy;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.queatz.branch.Branch;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.team.actions.AcceptJoinAction;
import com.queatz.snappy.team.actions.JoinPartyAction;
import com.queatz.snappy.team.contexts.ActivityContext;

/**
 * Created by jacob on 8/8/15.
 */
public class Background extends Service {
    Team team;

    @Override
    public void onCreate() {
        super.onCreate();

        team = ((MainApplication) getApplication()).team;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent == null)
            return Service.START_NOT_STICKY;

        String action = intent.getStringExtra(Config.EXTRA_ACTION);

        switch (action) {
            case Config.EXTRA_ACTION_JOIN_ACCEPT:
                Branch.from((ActivityContext) getApplicationContext())
                        .to(new AcceptJoinAction(intent.getStringExtra(Config.EXTRA_JOIN_ID)));
                break;
            case Config.EXTRA_ACTION_JOIN_REQUEST:
                Branch.from((ActivityContext) getApplicationContext())
                        .to(new JoinPartyAction(intent.getStringExtra(Config.EXTRA_PARTY_ID)));
                break;
        }


        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
