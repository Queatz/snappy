package com.queatz.snappy.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.lang.annotation.Documented;
import java.util.Date;

/**
 * Created by jacob on 10/19/14.
 */
public class SlideScreen extends ViewGroup {
    public abstract static class SlideScreenAdapter {
        public FragmentManager mFragmentManager;

        public SlideScreenAdapter(FragmentManager fragmentManager) {
            mFragmentManager = fragmentManager;
        }

        public abstract int getCount();
        public abstract Fragment getSlide(int slide);

        public FragmentManager getFragmentManager() {
            return mFragmentManager;
        }
    }

    private static class SlideAsChild {
        public int position;
        public Fragment fragment;

        public SlideAsChild(int p, Fragment f) {
            position = p;
            fragment = f;
        }
    }

    public interface OnSlideCallback {
        public void onSlide(int currentSlide, float offsetPercentage);
        public void onSlideChange(int currentSlide);
    }

    public static class SlideAnimation extends Handler {
        private SlideScreen mSlideScreen;
        private float mFrom;
        private float mTo;
        private int mDuration;
        private long mStartOffsetTime;
        private Date mStartTime;
        private Runnable mLoopRunnable;
        private boolean mAlive;

        public SlideAnimation(SlideScreen slideScreen, int to) {
            mSlideScreen = slideScreen;
            mFrom = mSlideScreen.mOffset;
            mTo = to;
            mDuration = 150;
            mStartOffsetTime = 0;
            mAlive = false;

            mLoopRunnable = new Runnable() {
                @Override
                public void run() {
                    loop();
                }
            };
        }

        public boolean isAlive() {
            return mAlive;
        }

        public void start() {
            start(0);
        }

        public void start(float startDelta) {
            mStartTime = new Date();
            mStartOffsetTime = (long) (mDuration * startDelta);
            mAlive = true;
            post(mLoopRunnable);
        }

        public void stop() {
            mAlive = false;
            removeCallbacks(mLoopRunnable);
        }

        public float interpolate(float dt) {
            return (float) Math.abs(Math.sin(dt * Math.PI / 2f));
        }

        public float getDelta() {
            return Math.min(1.0f, Math.max(0.0f, (float) (new Date().getTime() + mStartOffsetTime - mStartTime.getTime()) / (float) mDuration));
        }

        private void loop() {
            float dt = getDelta();

            apply(interpolate(dt));

            if(dt >= 1.0f) {
                stop();
                return;
            }

            postDelayed(mLoopRunnable, 0);
        }

        public void apply(float time){
            mSlideScreen.setOffset(mTo * time + mFrom * (1.0f - time));
        }
    }

    private Context mContext;
    private SparseArray<SlideAsChild> mSlides;
    private int mSlide;
    private float mOffset;
    private OnSlideCallback mOnSlideCallback;
    private SlideScreenAdapter mAdapter;
    private SlideAnimation mAnimation;
    private float mXFlingDelta;

    public SlideScreen(Context context) {
        super(context);
        init(context);
    }

    public SlideScreen(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SlideScreen(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        mSlides = new SparseArray<SlideAsChild>();
    }

    public void setOnSlideCallback(OnSlideCallback onSlideCallback) {
        mOnSlideCallback = onSlideCallback;
    }

    public void setAdapter(SlideScreenAdapter adapter) {
        mAdapter = adapter;
        populate();
    }

    public void setSlide(int slide) {
        smoothSlideTo(slide);
    }

    public void smoothSlideTo(int slide) {
        if(mAnimation != null) {
            mAnimation.stop();
        }
        mAnimation = new SlideAnimation(this, slide);
        mAnimation.start();

        mOnSlideCallback.onSlideChange(slide);
    }

    private void setOffset(float offset) {
        mOffset = Math.max(0, Math.min(mAdapter.getCount() - 1, offset));
        mSlide = Math.round(mOffset);
        positionChildren();

        if(mOnSlideCallback != null) {
            mOnSlideCallback.onSlide(mSlide, mOffset);
        }
    }

    private String getFragName(Object id, Object slide) {
        return "slidescreen:" + getId() + ":" + slide;
    }

    private void populate() {
        removeAllViews();

        FragmentTransaction transaction = mAdapter.getFragmentManager().beginTransaction();

        for(int slide = 0; slide < mAdapter.getCount(); slide++) {
            Fragment fragment = mAdapter.getSlide(slide);
            mSlides.append(slide, new SlideAsChild(slide, fragment));
            transaction.add(getId(), fragment, getFragName(getId(), slide));
        }

        transaction.commitAllowingStateLoss();
    }

    private SlideAsChild slideFromView(View view) {
        for(int x = 0; x < mSlides.size(); x++) {
            SlideAsChild child = mSlides.valueAt(x);
            if(child.fragment.getView() == view)
                return child;
        }

        return null;
    }

    private void positionChildren() {
        int fr = (int) Math.floor(mOffset);
        int to = fr + 1;

        int width = getWidth();

        for(int c = 0; c < getChildCount(); c++) {
            View view = getChildAt(c);
            SlideAsChild child = slideFromView(view);

            view.setVisibility(child.position < fr || child.position > to ? View.GONE : View.VISIBLE);
            int l = width * child.position - (int) (mOffset * (float) getWidth());
            view.layout(l, 0, l + getWidth(), getHeight());
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        positionChildren();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        for(int c = 0; c < getChildCount(); c++) {
            getChildAt(c).measure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(mAnimation != null && mAnimation.isAlive())
            mAnimation.stop();

        float xdiff = 0;

        if(event.getHistorySize() > 0) {
            xdiff = event.getX() - event.getHistoricalX(0);
        }

        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            mXFlingDelta = 0;
        }
        else if(event.getAction() == MotionEvent.ACTION_MOVE) {
            setOffset(mOffset - xdiff / getWidth());

            if(event.getHistorySize() > 0) {
                int h = Math.min(10, event.getHistorySize() - 1);
                mXFlingDelta = (
                        (event.getHistoricalX(h) - event.getX()) /
                        ((float) (event.getEventTime() - event.getHistoricalEventTime(h)) / 1000.0f)
                );
            }
        }
        else if(event.getAction() == MotionEvent.ACTION_UP) {
                int slide;

                if(Math.abs(mXFlingDelta) > 15) {
                    if(mXFlingDelta < 0)
                        slide = (int) Math.floor(mOffset);
                    else
                        slide = (int) Math.ceil(mOffset);
                }
                else
                    slide = Math.round(mOffset);

            setSlide(slide);
        }

        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if(event.getHistorySize() < 1)
            return false;

        float xdif = event.getHistoricalX(event.getHistorySize() - 1) - event.getX();
        float ydif = event.getHistoricalY(event.getHistorySize() - 1) - event.getY();

        return Math.abs(xdif) > Math.abs(ydif) && Math.abs(xdif) > 8;
    }
}
