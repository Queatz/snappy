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
        super.onCreate();
        Global.setupWithContext(this);
        Util.setupWithContext(this);

        team = new Team(this);
        Util.team = team;
    }

    @Override
    public void onTerminate() {
        team.close();
        super.onTerminate();
    }
}
