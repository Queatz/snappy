package com.queatz.snappy;

import android.app.Application;

import com.queatz.snappy.team.Team;
import com.queatz.snappy.ui.Global;

/**
 * Created by jacob on 10/18/14.
 */
public class MainApplication extends Application {
    public Team team;

    @Override
    public void onCreate() {
        Global.setupWithContext(this);

        team = new Team(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}
