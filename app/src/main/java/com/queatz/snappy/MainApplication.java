package com.queatz.snappy;

import android.app.Application;

import com.queatz.snappy.ui.Global;

/**
 * Created by jacob on 10/18/14.
 */
public class MainApplication extends Application {
    @Override
    public void onCreate() {
        Global.setupWithContext(this);
    }
}
