package com.queatz.snappy.team;

import android.content.Context;
import android.content.SharedPreferences;

import com.queatz.snappy.MainActivity;
import com.queatz.snappy.activity.ViewActivity;

/**
 * Created by jacob on 10/19/14.
 */
public class Team {
    public Context context;
    public Auth auth;
    public MainActivity view;
    public SharedPreferences preferences;

    public Team(Context c) {
        context = c;
        auth = new Auth(this);
        preferences = c.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
    }

    public void load() {
        auth.load();
    }

    public void save() {
        auth.save();
    }
}
