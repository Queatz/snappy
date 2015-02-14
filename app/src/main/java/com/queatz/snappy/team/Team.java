package com.queatz.snappy.team;

import android.content.Context;
import android.content.SharedPreferences;

import com.queatz.snappy.MainActivity;

/**
 * Created by jacob on 10/19/14.
 */
public class Team {
    public Context context;
    public Auth auth;
    public MainActivity view;
    public SharedPreferences preferences;
    public Api api;
    public Action action;
    public Things things;

    public Team(Context c) {
        context = c;
        preferences = c.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        auth = new Auth(this);
        api = new Api(this);
        action = new Action(this);
        things = new Things(this);
    }
}
