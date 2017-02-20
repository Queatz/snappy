package com.queatz.snappy.team;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.queatz.snappy.activity.Main;
import com.queatz.snappy.activity.Welcome;

/**
 * Created by jacob on 2/20/15.
 */
public class View {
    public Team team;

    private String mTop = null;

    public View(Team t) {
        team = t;
    }

    public void show(@Nullable Activity from, Class<? extends Activity> activity, Bundle bundle) {
        Context context = from == null ? team.context : from;

        Intent intent = new Intent(context, activity);

        if(bundle != null)
            intent.putExtras(bundle);

        if(from == null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }

        context.startActivity(intent);
    }

    public void showStartView(@Nullable Activity from) {
        team.view.show(from, team.auth.isAuthenticated() ? Main.class : Welcome.class, null);
    }

    public void setTop(String top) {
        mTop = top;
    }

    public void clearTop(String top) {
        if(top != null && top.equals(getTop())) {
            mTop = null;
        }
    }

    public String getTop() {
        return mTop;
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

    public void keyboard(IBinder windowToken) {
        InputMethodManager inputMethodManager = (InputMethodManager) team.context.getSystemService(Service.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(windowToken, 0);
    }
}
