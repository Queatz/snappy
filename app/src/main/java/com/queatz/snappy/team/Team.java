package com.queatz.snappy.team;

import android.content.Context;

import com.queatz.snappy.activity.ViewActivity;

/**
 * Created by jacob on 10/19/14.
 */
public class Team {
    public Context context;
    public Auth auth;
    public ViewActivity view;

    public Team(Context c) {
        context = c;
        auth = new Auth(this);
    }
}
