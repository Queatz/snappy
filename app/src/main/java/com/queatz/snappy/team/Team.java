package com.queatz.snappy.team;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;

import com.queatz.snappy.shared.Config;

import java.io.Closeable;
import java.util.Date;

import io.realm.DynamicRealm;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;

/**
 * Created by jacob on 10/19/14.
 */
public class Team implements Closeable {
    public Context context;
    public DynamicRealm realm;
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
    public Here here;
    public Advertise advertise;
    public Camera camera;

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
        here = new Here(this);
        advertise = new Advertise(this);
        camera = new Camera(this);
    }

    public void close() {
        realm.close();
    }

    public void wipe() {
        Realm.deleteRealm(new RealmConfiguration.Builder().build());
    }

    public DynamicRealm realm() {
        Realm.init(context);

        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            int prefAppVersion = preferences.getInt(Config.PREFERENCE_APP_VERSION, -1);
            int realAppVersion = pInfo.versionCode;

            if (prefAppVersion < Config.WIPE_VERSIONS_BELOW) {
                wipe();
            }

            if (realAppVersion != prefAppVersion) {
                preferences.edit().putInt(Config.PREFERENCE_APP_VERSION, realAppVersion).apply();
            }
        }
        catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        DynamicRealm dynamicRealm = DynamicRealm.getInstance(new RealmConfiguration.Builder()
            .migration(new RealmMigration() {
                @Override
                public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {

                }
            })
            .deleteRealmIfMigrationNeeded().build());

        if (dynamicRealm.getSchema().contains("Thing")) {
            return dynamicRealm;
        }

        dynamicRealm.beginTransaction();

        RealmObjectSchema schema = dynamicRealm.getSchema().create("Thing");

        // String
        schema.addField(Thing.ID, String.class);
        schema.addField(Thing.NAME, String.class);
        schema.addField(Thing.ABOUT, String.class);
        schema.addField(Thing.KIND, String.class);
        schema.addField(Thing.ADDRESS, String.class);
        schema.addField(Thing.UNIT, String.class);
        schema.addField(Thing.STATUS, String.class);
        schema.addField(Thing.ACTION, String.class);
        schema.addField(Thing.FIRST_NAME, String.class);
        schema.addField(Thing.LAST_NAME, String.class);
        schema.addField(Thing.IMAGE_URL, String.class);
        schema.addField(Thing.GOOGLE_URL, String.class);
        schema.addField(Thing.AUTH, String.class);
        schema.addField(Thing.MESSAGE, String.class);
        schema.addField(Thing.SOCIAL_MODE, String.class);
        schema.addField(Thing.PLACEHOLDER, String.class);

        // Object
        schema.addRealmObjectField(Thing.LOCATION, schema);
        schema.addRealmObjectField(Thing.SOURCE, schema);
        schema.addRealmObjectField(Thing.TARGET, schema);
        schema.addRealmObjectField(Thing.PERSON, schema);
        schema.addRealmObjectField(Thing.LATEST, schema);
        schema.addRealmObjectField(Thing.HOST, schema);
        schema.addRealmObjectField(Thing.FROM, schema);
        schema.addRealmObjectField(Thing.TO, schema);

        // Date
        schema.addField(Thing.CREATED_ON, Date.class);
        schema.addField(Thing.INFO_UPDATED, Date.class);
        schema.addField(Thing.DATE, Date.class);
        schema.addField(Thing.UPDATED, Date.class);

        // Boolean
        schema.addField(Thing.PHOTO, Boolean.class);
        schema.addField(Thing.SEEN, Boolean.class);
        schema.addField(Thing.FULL, Boolean.class);
        schema.addField(Thing.WANT, Boolean.class);
        schema.addField(Thing.GOING, Boolean.class);

        // Double
        schema.addField(Thing.LATITUDE, Double.class);
        schema.addField(Thing.LONGITUDE, Double.class);
        schema.addField(Thing.INFO_DISTANCE, Double.class);
        schema.addField(Thing.ASPECT, Double.class);

        // Integer
        schema.addField(Thing.PRICE, Integer.class);
        schema.addField(Thing.INFO_FOLLOWERS, Integer.class);
        schema.addField(Thing.INFO_FOLLOWING, Integer.class);
        schema.addField(Thing.LIKERS, Integer.class);

        // List
        schema.addRealmListField(Thing.OFFERS, schema);
        schema.addRealmListField(Thing.JOINS, schema);
        schema.addRealmListField(Thing.UPDATES, schema);
        schema.addRealmListField(Thing.CONTACTS, schema);
        schema.addRealmListField(Thing.WITH, schema);

        // Local only
        schema.addField(Thing.LOCAL_STATE, String.class);

        dynamicRealm.commitTransaction();

        return dynamicRealm;
    }

    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        auth.onActivityResult(requestCode, resultCode, data);
        buy.onActivityResult(activity, requestCode, resultCode, data);
        location.onActivityResult(requestCode, resultCode, data);
        action.onActivityResult(activity, requestCode, resultCode, data);
        advertise.onActivityResult(activity, requestCode, resultCode, data);
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
       if (requestCode == Config.REQUEST_CODE_REQUEST_PERMISSION) {
           for (int i = 0; i < permissions.length; i++) {
               if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                   location.onPermissionGranted(permissions[0]);
                   camera.onPermissionGranted(permissions[0]);
               }
           }
       }
    }
}
