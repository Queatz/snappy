package com.queatz.snappy.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageButton;

/**
 * Created by jacob on 10/18/14.
 */
public class SignInButton extends ImageButton {
    public SignInButton(Context context) {
        super(context);
        init();
    }

    public SignInButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SignInButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
    }
}
