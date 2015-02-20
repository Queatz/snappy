package com.queatz.snappy.team;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;

import com.queatz.snappy.activity.Main;
import com.queatz.snappy.activity.ViewActivity;
import com.queatz.snappy.activity.Welcome;

/**
 * Created by jacob on 2/20/15.
 */
public class View {
    public Team team;

    public View(Team t) {
        team = t;
    }

    public void show(ViewActivity.Transition in, ViewActivity.Transition out, Activity from, Class<? extends Activity> activity, Bundle bundle) {
        Intent intent = new Intent(from, activity);

        if(bundle != null)
            intent.putExtras(bundle);

        //Bundle options = ActivityOptions.makeCustomAnimation(from, 0, 0).toBundle();

        from.startActivity(intent/*, options*/);
    }

    public void showStartView(Activity from) {
        team.view.show(null, null, from, team.auth.isAuthenticated() ? Main.class : Welcome.class, null);
    }
}
