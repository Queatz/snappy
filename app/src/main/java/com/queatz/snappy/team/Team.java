package com.queatz.snappy.team;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.Closeable;

import io.realm.Realm;

/**
 * Created by jacob on 10/19/14.
 */
public class Team implements Closeable {
    public Context context;
    public Realm realm;
    public SharedPreferences preferences;
    public View view;
    public Auth auth;
    public Api api;
    public Action action;
    public Things things;

    public Team(Context c) {
        context = c;
        realm = realm();
        preferences = c.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        auth = new Auth(this);
        api = new Api(this);
        action = new Action(this);
        things = new Things(this);
        view = new View(this);
    }

    public void close() {
        realm.close();
    }

    public Realm realm() {
        return Realm.getInstance(context);
    }
}
