package com.queatz.snappy.ui;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Created by jacob on 8/29/15.
 */
public class SwipeRefreshLayout extends android.support.v4.widget.SwipeRefreshLayout {
    public SwipeRefreshLayout(Context context) {
        this(context, null);
    }

    public SwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        if(getParent() != null) {
            getParent().requestDisallowInterceptTouchEvent(disallowIntercept);
        }
    }

}
