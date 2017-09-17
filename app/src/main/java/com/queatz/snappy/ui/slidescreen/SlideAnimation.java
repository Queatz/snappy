package com.queatz.snappy.ui.slidescreen;

import android.os.Handler;

import java.util.Date;

/**
 * Created by jacob on 9/17/17.
 */
public class SlideAnimation extends Handler {
    private SlideScreen mSlideScreen;
    private float mFrom;
    private float mTo;
    private int mDuration;
    private long mStartOffsetTime;
    private Date mStartTime;
    private Runnable mLoopRunnable;
    private boolean mAlive;

    SlideAnimation(SlideScreen slideScreen, int to) {
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

    boolean isAlive() {
        return mAlive;
    }

    void start() {
        start(0);
    }

    void stop() {
        mAlive = false;
        removeCallbacks(mLoopRunnable);
    }

    private void start(float startDelta) {
        mStartTime = new Date();
        mStartOffsetTime = (long) (mDuration * startDelta);
        mAlive = true;
        post(mLoopRunnable);
    }

    private float interpolate(float dt) {
        return (float) Math.abs((Math.sin((dt - .5) * Math.PI) + 1) / 2f);
    }

    private float getDelta() {
        return Math.min(1.0f, Math.max(0.0f, (float) (new Date().getTime() + mStartOffsetTime - mStartTime.getTime()) / (float) mDuration));
    }

    private void loop() {
        float dt = getDelta();

        apply(interpolate(dt));

        if (dt >= 1.0f) {
            stop();
            return;
        }

        postDelayed(mLoopRunnable, 0);
    }

    private void apply(float time) {
        mSlideScreen.setOffset(mTo * time + mFrom * (1.0f - time));
    }
}
