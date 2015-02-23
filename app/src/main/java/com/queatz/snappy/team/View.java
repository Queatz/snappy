package com.queatz.snappy.team;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.queatz.snappy.activity.Main;
import com.queatz.snappy.activity.Welcome;

/**
 * Created by jacob on 2/20/15.
 */
public class View {
    public Team team;

    public View(Team t) {
        team = t;
    }

    public void show(Activity from, Class<? extends Activity> activity, Bundle bundle) {
        Intent intent = new Intent(from, activity);

        if(bundle != null)
            intent.putExtras(bundle);

        //Bundle options = ActivityOptions.makeCustomAnimation(from, android.R.anim.slide_in_left, android.R.anim.fade_out).toBundle();

        from.startActivity(intent/*, options*/);
    }

    public void showStartView(Activity from) {
        team.view.show(from, team.auth.isAuthenticated() ? Main.class : Welcome.class, null);
    }
}
