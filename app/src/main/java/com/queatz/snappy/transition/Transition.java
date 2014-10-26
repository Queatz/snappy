package com.queatz.snappy.transition;

import android.app.Fragment;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import com.queatz.snappy.Config;
import com.queatz.snappy.ui.Global;

import java.util.Date;

/**
 * Created by jacob on 10/25/14.
 */
public abstract class Transition extends Handler {
    public static enum Direction {
        IN,
        OUT
    }

    public static interface OnCompleteCallback {
        public void onComplete(Fragment fragment);
    }

    protected Fragment mFrag;
    protected Direction mDirection;
    protected float mDuration;

    private float mTime;
    private Date mStartTime;

    private Runnable mRunnable;
    private OnCompleteCallback mOnCompleteCallback;

    public Transition() {
        mFrag = null;
        mTime = 0;
        mDuration = 300;
        mStartTime = new Date();
        mDirection = Direction.IN;

        mRunnable = new Runnable() {
            @Override
            public void run() {
                loop();
            }
        };
        mOnCompleteCallback = null;
    }

    private void loop() {
        mTime = Math.min(1.0f, Math.max(0.0f, (new Date().getTime() - mStartTime.getTime()) / mDuration));

        onDraw(mFrag, mTime, mDirection);

        if(mTime < 1.0f) {
            postDelayed(mRunnable, 2);
        }
        else {
            if(mOnCompleteCallback != null)
                mOnCompleteCallback.onComplete(mFrag);
        }
    }

    public Transition fragment(Fragment fragment) {
        mFrag = fragment;

        return this;
    }

    public Transition onComplete(OnCompleteCallback onCompleteCallback) {
        mOnCompleteCallback = onCompleteCallback;

        return this;
    }

    public Transition duration(int milliseconds) {
        mDuration = milliseconds;

        return this;
    }

    public float interpolate(float dt) {
        return (float) Math.abs((Math.sin((dt - .5) * Math.PI) + 1) / 2f);
    }

    public void in() {
        start(Direction.IN);
    }

    public void out() {
        start(Direction.OUT);
    }

    public void start(Direction direction) {
        if(mFrag == null) {
            Log.w(Config.TAG, "Must call .fragment() first.");
            return;
        }

        mDirection = direction;
        post(mRunnable);
    }

    public void cancel() {
        removeCallbacks(mRunnable);
    }

    public abstract void onDraw(Fragment fragment, float interpolatedTime, Direction direction);
}
