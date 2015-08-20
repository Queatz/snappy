package com.queatz.snappy.team;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.queatz.snappy.Config;

import java.io.Closeable;

import io.realm.Realm;
import io.realm.RealmConfiguration;

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
    public Location location;
    public Push push;
    public Local local;
    public Buy buy;
    public Menu menu;

    public Team(Context c) {
        context = c;
        preferences = c.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        realm = realm();
        buy = new Buy(this);
        auth = new Auth(this);
        api = new Api(this);
        action = new Action(this);
        things = new Things(this);
        view = new View(this);
        location = new Location(this);
        push = new Push(this);
        local = new Local(this);
        menu = new Menu(this);
    }

    public void close() {
        realm.close();
    }

    public void wipe() {
        Realm.deleteRealm(new RealmConfiguration.Builder(context).build());
    }

    public Realm realm() {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            int prefAppVersion = preferences.getInt(Config.PREFERENCE_APP_VERSION, -1);
            int realAppVersion = pInfo.versionCode;

            if (prefAppVersion < 9) {
                wipe();
            }

            if (realAppVersion != prefAppVersion) {
                preferences.edit().putInt(Config.PREFERENCE_APP_VERSION, realAppVersion).apply();
            }
        }
        catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return Realm.getInstance(context);
    }

    public void db(@NonNull final Db.Call dbCall) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                dbCall.db(realm());

                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                dbCall.post();
            }
        }.execute();
    }
}
