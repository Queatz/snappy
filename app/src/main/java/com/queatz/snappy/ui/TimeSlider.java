package com.queatz.snappy.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.queatz.snappy.R;
import com.queatz.snappy.Util;

/**
 * Created by jacob on 2/21/15.
 */
public class TimeSlider extends RelativeLayout {
    public static interface TextCallback {
        public String getText(float percent);
    }

    private TextView mHandle;
    private View mTrack;
    private TextCallback mTextCallback;
    private float mPercent;

    public TimeSlider(Context context) {
        super(context);
        init(null, 0);
    }

    public TimeSlider(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public TimeSlider(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        mHandle = new TextView(getContext());
        mTrack = new View(getContext());

        if(attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.TimeSlider, defStyle, 0);

            if(a.hasValue(R.styleable.TimeSlider_textColor))
                mHandle.setTextColor(a.getColor(R.styleable.TimeSlider_textColor, -1));

            if(a.hasValue(R.styleable.TimeSlider_textSize))
                mHandle.setTextSize(TypedValue.COMPLEX_UNIT_PX, a.getDimensionPixelSize(R.styleable.TimeSlider_textSize, -1));

            if(a.hasValue(R.styleable.TimeSlider_handle))
                mHandle.setBackgroundResource(a.getResourceId(R.styleable.TimeSlider_handle, -1));

            a.recycle();
        }

        setClickable(true);
        setScrollContainer(true);
        setFocusable(true);
        setOverScrollMode(View.OVER_SCROLL_NEVER);
        int p = (int) Util.px(16);

        mTrack.setBackgroundColor(mHandle.getTextColors().getDefaultColor());
        mTrack.setAlpha(0.125f);
        addView(mTrack);

        mHandle.setPadding(p, p, p, p);
        addView(mHandle);

        setPercent(0);
    }

    public void setTextCallback(TextCallback textCallback) {
        mTextCallback = textCallback;
        setPercent(mPercent);
    }

    public float getPercent() {
        return mPercent;
    }

    public void setPercent(float percent) {
        mPercent = Math.min(1, Math.max(0, percent));

        if(mTextCallback != null) {
            mHandle.setText(mTextCallback.getText(mPercent));
        }
        else {
            mHandle.setText(Integer.toString((int) mPercent * 100));
        }

        if(getMeasuredWidth() > 0) {
            mHandle.measure(
                    LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT);
        }

        LayoutParams params = (LayoutParams) mHandle.getLayoutParams();
        params.leftMargin = Math.min(getMeasuredWidth() - mHandle.getMeasuredWidth(), Math.max(0, (int) (getMeasuredWidth() * percent - mHandle.getMeasuredWidth() / 2)));
        mHandle.setLayoutParams(params);
    }

    private void updateTrack() {
        LayoutParams params = (LayoutParams) mTrack.getLayoutParams();

        if(params == null)
            params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        params.leftMargin = (int) Util.px(32);
        params.rightMargin = (int) Util.px(32);
        params.topMargin = (int) (getMeasuredHeight() / 1.85);
        params.height = (int) Util.px(2);

        mTrack.setLayoutParams(params);
    }

    @Override
    public void onSizeChanged(int w, int h, int ow, int oh) {
        setPercent(mPercent);
        updateTrack();
    }

    @Override
    public void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        if(changed) {
            setPercent(mPercent);
            updateTrack();
        }
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        if (getParent() != null) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                setPercent(event.getX() / getWidth());
                return true;
            default:
                return true;
        }
    }
}
