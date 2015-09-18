package com.queatz.snappy.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by jacob on 8/29/15.
 */
public class SwipeRefreshLayout extends android.support.v4.widget.SwipeRefreshLayout {
    private boolean mChildIsUsingMotion;

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

        mChildIsUsingMotion = disallowIntercept;
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mChildIsUsingMotion = false;

                break;
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                if(mChildIsUsingMotion) {
                    return false;
                }

                break;
        }

        return super.onInterceptTouchEvent(event);
    }
}
