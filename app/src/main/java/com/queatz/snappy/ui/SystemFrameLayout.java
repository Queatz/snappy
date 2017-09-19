package com.queatz.snappy.ui;

import android.content.Context;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.WindowInsets;
import android.widget.RelativeLayout;

/**
 * Created by jacob on 9/18/17.
 */

public class SystemFrameLayout extends RelativeLayout {
    public SystemFrameLayout(@NonNull Context context) {
        super(context);
        init();
    }

    public SystemFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SystemFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setFitsSystemWindows(true);
    }

    @Override
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            setPadding(0, 0, 0, insets.getSystemWindowInsetBottom());
        }

        return insets;
    }
}
