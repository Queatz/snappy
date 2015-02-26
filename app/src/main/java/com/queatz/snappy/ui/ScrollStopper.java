package com.queatz.snappy.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

/**
 * Created by jacob on 2/25/15.
 */
public class ScrollStopper extends RelativeLayout {

    public ScrollStopper(Context context) {
        super(context);
        init(null, 0);
    }

    public ScrollStopper(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public ScrollStopper(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {

    }

    @Override
    public boolean onInterceptTouchEvent(@NonNull MotionEvent event) {
        if (getParent() != null) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }

        return false;
    }
}
