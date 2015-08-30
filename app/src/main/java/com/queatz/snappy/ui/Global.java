package com.queatz.snappy.ui;

import android.content.Context;
import android.graphics.Typeface;

/**
 * Created by jacob on 10/18/14.
 */
public class Global {
    public static Typeface defaultFont = null;

    public static void setupWithContext(Context context) {
        defaultFont = Typeface.DEFAULT;
    }
}
