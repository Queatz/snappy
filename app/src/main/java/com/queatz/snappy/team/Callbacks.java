package com.queatz.snappy.team;

import android.app.Activity;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.util.Log;

import com.queatz.snappy.shared.Config;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jacob on 9/18/17.
 */

public class Callbacks {
    private final Team team;
    private final Map<Integer, PreferenceManager.OnActivityResultListener> callbacks;

    public Callbacks(Team team) {
        this.team = team;
        callbacks = new HashMap<>();
    }

    public void set(int requestCode, PreferenceManager.OnActivityResultListener onActivityResultListener) {
        if (!callbacks.containsKey(requestCode)) {
            callbacks.put(requestCode, onActivityResultListener);
        } else {
            Log.e(Config.LOG_TAG, "Cannot override callback for code: " + requestCode);
        }
    }

    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        if (callbacks.containsKey(requestCode)) {
            callbacks.remove(requestCode).onActivityResult(requestCode, resultCode, data);
        }
    }
}
