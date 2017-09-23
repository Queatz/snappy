package com.queatz.snappy.team;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.queatz.snappy.MainApplication;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by jacob on 9/21/17.
 */

public class Stack implements Application.ActivityLifecycleCallbacks {

    private final Team team;
    private Set<Activity> activities = new HashSet<>();

    public Stack(Team team) {
        this.team = team;

        ((MainApplication) this.team.context.getApplicationContext()).registerActivityLifecycleCallbacks(this);
    }

    public void closeAllExcept(@NonNull Class<? extends Activity> activityClass) {
        for (Activity activity : activities) {
            if (activityClass.isAssignableFrom(activity.getClass())) {
                continue;
            }

            activity.finish();
        }
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        activities.add(activity);
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        activities.remove(activity);
    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }
}
