package com.queatz.snappy.team;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.queatz.snappy.Config;
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

        from.startActivity(intent);
    }

    public void showStartView(Activity from) {
        team.view.show(from, team.auth.isAuthenticated() ? Main.class : Welcome.class, null);
    }

    public void setTop(String top) {
        team.preferences.edit().putString(Config.PREFERENCE_GCM_TOP_ACTIVITY, top).apply();
    }

    public void clearTop(String top) {
        if(top != null && top.equals(getTop()))
            team.preferences.edit().putString(Config.PREFERENCE_GCM_TOP_ACTIVITY, null).apply();
    }

    public String getTop() {
        return team.preferences.getString(Config.PREFERENCE_GCM_TOP_ACTIVITY, null);
    }

    public void keyboard(TextView view) {
        keyboard(view, true);
    }

    public void keyboard(TextView view, boolean show) {
        InputMethodManager inputMethodManager = (InputMethodManager) team.context.getSystemService(Service.INPUT_METHOD_SERVICE);

        if(show)
            inputMethodManager.showSoftInput(view, 0);
        else
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
