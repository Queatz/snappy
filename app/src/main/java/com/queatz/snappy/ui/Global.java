package com.queatz.snappy.ui;

import android.content.Context;
import android.graphics.Typeface;

/**
 * Created by jacob on 10/18/14.
 */
public class Global {
    public static String LOG_TAG = "SNAPPYLOG";

    public static Typeface defaultFont;

    public static void setupWithContext(Context context) {
        defaultFont = Typeface.createFromAsset(context.getAssets(), "Calibri.ttf");
    }
}
