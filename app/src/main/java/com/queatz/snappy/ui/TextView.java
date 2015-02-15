package com.queatz.snappy.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;

/**
 * Created by jacob on 10/18/14.
 */
public class TextView extends android.widget.TextView {
    public TextView(Context context) {
        super(context);
        init();
    }

    public TextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        if(Global.defaultFont == null) {
            return;
        }

        setTypeface(Global.defaultFont, getTypeface() == null ? Typeface.NORMAL : getTypeface().getStyle());
    }
}
