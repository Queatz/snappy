package com.queatz.snappy.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.EditText;

/**
 * Created by jacob on 10/18/14.
 */
public class FloatingSearch extends EditText {
    public FloatingSearch(Context context) {
        super(context);
        init();
    }

    public FloatingSearch(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FloatingSearch(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        if(Global.defaultFont == null) {
            Log.w(Global.LOG_TAG, "Default font not set up!");
            return;
        }

        setTypeface(Global.defaultFont);
    }
}
