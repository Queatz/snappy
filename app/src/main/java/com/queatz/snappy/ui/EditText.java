package com.queatz.snappy.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

/**
 * Created by jacob on 2/14/15.
 */
public class EditText extends android.widget.EditText {
    public EditText(Context context) {
        super(context);
        init();
    }

    public EditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public EditText(Context context, AttributeSet attrs, int defStyle) {
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
