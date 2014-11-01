package com.queatz.snappy;

import android.content.Context;
import android.util.TypedValue;

/**
 * Created by jacob on 10/31/14.
 */
public class Util {
    public static float px(Context context, float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }
    public static float dp(Context context, float px) {
        return px / context.getResources().getDisplayMetrics().density;
    }
}
