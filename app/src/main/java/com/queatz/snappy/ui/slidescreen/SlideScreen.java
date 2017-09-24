package com.queatz.snappy.ui.slidescreen;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.queatz.snappy.MainApplication;
import com.queatz.snappy.Util;
import com.queatz.snappy.shared.Config;

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
        void onSlide(int currentSlide, float offsetPercentage);
        void onSlideChange(int currentSlide);
    }

    private SparseArray<SlideAsChild> mSlides;
    private int mSlide;
    protected float mOffset;
    private OnSlideCallback mOnSlideCallback;
    private SlideScreenAdapter mAdapter;
    private SlideAnimation mAnimation;
    private ExposeAnimation exposeAnimation;
    private float mXFlingDelta;
    private float mDownX, mDownY;
    private boolean mSnatched, mUnsnatchable;
    private boolean mChildIsUsingMotion;
    private int slopRadius = (int) Util.px(96);
    private int gap = (int) Util.px(128);
    protected boolean expose = false;
    protected float currentScale = 1;

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
        mSlides = new SparseArray<>();
        mSnatched = false;
        mUnsnatchable = false;
    }

    public void setOnSlideCallback(OnSlideCallback onSlideCallback) {
        mOnSlideCallback = onSlideCallback;
    }

    public void setAdapter(SlideScreenAdapter adapter) {
        mAdapter = adapter;
        populate();
    }

    public SlideScreenAdapter getAdapter() {
        return mAdapter;
    }

    public void setSlide(int slide) {
        // When setting slide, close any keyboards that are open
        ((MainApplication) getContext().getApplicationContext()).team.view.keyboard(getWindowToken());

        smoothSlideTo(slide);
    }

    public int getSlide() {
        return mSlide;
    }

    public Fragment getSlideFragment(int slide) {
        return mSlides.get(slide).fragment;
    }

    public void expose(boolean expose) {
        this.expose = expose;

        if (expose && currentScale >= 1) {
            // Change background color
        }

        if(exposeAnimation != null) {
            exposeAnimation.stop();
        }

        exposeAnimation = new ExposeAnimation(this, this.expose);
        exposeAnimation.start();
    }

    public boolean isExpose() {
        return expose;
    }

    private void smoothSlideTo(int slide) {
        if(mAnimation != null) {
            mAnimation.stop();
        }
        mAnimation = new SlideAnimation(this, slide);
        mAnimation.start();

        if(mOnSlideCallback != null)
            mOnSlideCallback.onSlideChange(slide);
    }

    protected void setOffset(float offset) {
        mOffset = Math.max(0, Math.min(mAdapter.getCount() - 1, offset));
        mSlide = Math.round(mOffset);
        positionChildren();

        if(mOnSlideCallback != null) {
            mOnSlideCallback.onSlide(mSlide, mOffset);
        }
    }

    protected void setScale(float scale) {
        this.currentScale = scale;
        positionChildren();
    }

    private String getFragName(Object slide) {
        String n = "slidescreen:" + getId() + ":" + slide;
        Log.e(Config.LOG_TAG, n);
        return n;
    }

    private void populate() {
        removeAllViews();

        FragmentTransaction transaction = mAdapter.getFragmentManager().beginTransaction();

        for(int slide = 0; slide < mAdapter.getCount(); slide++) {
            Fragment fragment = mAdapter.getSlide(slide);
            mSlides.append(slide, new SlideAsChild(slide, fragment));
            transaction.add(getId(), fragment, getFragName(slide));
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

        int width = getWidth() + (int) (gap * (1 - currentScale));

        for(int c = 0; c < getChildCount(); c++) {
            View view = getChildAt(c);
            SlideAsChild child = slideFromView(view);

            if(child == null)
                continue;

            int previousVisibility = view.getVisibility();

            int expfrto = currentScale >= 1 ? 0 : 1;

            view.setVisibility(
                    (child.position < fr - expfrto) ||
                    (child.position > ( mOffset == 0 ? fr : to) + expfrto) ?
                    View.GONE :
                    View.VISIBLE
            );

            if (view.getVisibility() != previousVisibility) {
                if (previousVisibility == View.GONE) {
                    child.fragment.onResume();
                } else {
                    child.fragment.onPause();
                }
            }

            view.setScaleX(currentScale);
            view.setScaleY(currentScale);

            int l = (int) (currentScale * (width * child.position - (int) (mOffset * (float) width)));
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
        float xdiff = 0;

        if(event.getHistorySize() > 0) {
            xdiff = event.getX() - event.getHistoricalX(0);
        }

        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if(mAnimation != null && mAnimation.isAlive())
                    mAnimation.stop();

                mSnatched = false;
                mUnsnatchable = false;
                mDownX = event.getRawX();
                mDownY = event.getRawY();
                mXFlingDelta = 0;
                getParent().requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_MOVE:
                setOffset(mOffset - xdiff / getWidth());

                if (event.getHistorySize() > 0) {
                    int h = Math.min(10, event.getHistorySize() - 1);
                    mXFlingDelta = (
                            (event.getHistoricalX(h) - event.getX()) /
                                    ((float) (event.getEventTime() - event.getHistoricalEventTime(h)) / 1000.0f)
                    );
                }

                if(!mSnatched && !mUnsnatchable) {
                    if(shouldLetGo(event)) {
                        resolve(false);
                        getParent().requestDisallowInterceptTouchEvent(false);
                        return false;
                    }
                    else {
                        mSnatched = shouldSnatch(event);
                        mUnsnatchable = isUnsnatchable(event);

                        if(mSnatched) {
                            getParent().requestDisallowInterceptTouchEvent(true);
                        }
                    }
                }

                break;
            case MotionEvent.ACTION_UP:
                getParent().requestDisallowInterceptTouchEvent(false);

                if (expose && Math.abs(event.getX() - mDownX) < slopRadius && Math.abs(event.getY() - mDownY) < slopRadius) {
                    expose(false);
                }

                resolve(true);
                return false;
        }

        return true;
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        mChildIsUsingMotion = disallowIntercept;

        if(getParent() != null) {
            getParent().requestDisallowInterceptTouchEvent(disallowIntercept);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mSnatched = false;
                mUnsnatchable = false;
                mDownX = event.getRawX();
                mDownY = event.getRawY();
                mXFlingDelta = 0;

                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mChildIsUsingMotion = false;
                mUnsnatchable = false;
                mSnatched = false;

                break;
            case MotionEvent.ACTION_MOVE:
                if(mSnatched || mChildIsUsingMotion || mUnsnatchable)
                    return mSnatched;

                mSnatched = shouldSnatch(event);
                mUnsnatchable = isUnsnatchable(event);

                break;
        }

        return mSnatched;
    }

    private boolean isUnsnatchable(MotionEvent event) {
        if (mSnatched) {
            return false;
        }

        float xdif = event.getRawX() - mDownX;
        float ydif = event.getRawY() - mDownY;

        return Math.abs(xdif) < Math.abs(ydif) && Math.abs(ydif) > 16;
    }

    private boolean shouldSnatch(MotionEvent event) {
        float xdif = event.getRawX() - mDownX;
        float ydif = event.getRawY() - mDownY;

        boolean edged = (xdif < 0 && mSlide >= mAdapter.getCount() - 1) ||
                (xdif > 0 && mSlide <= 0);

        return Math.abs(xdif) > Math.abs(ydif) && Math.abs(xdif) > 16 && !edged;
    }

    private boolean shouldLetGo(MotionEvent event) {
        float xdif = event.getRawX() - mDownX;
        float ydif = event.getRawY() - mDownY;
        return Math.abs(ydif) > Math.abs(xdif) && Math.abs(ydif) > 16;
    }

    private void resolve(boolean fling) {
        mSnatched = false;

        int slide;

        if (fling && Math.abs(mXFlingDelta) > 15) {
            if (mXFlingDelta < 0)
                slide = (int) Math.floor(mOffset);
            else
                slide = (int) Math.ceil(mOffset);
        } else {
            slide = Math.round(mOffset);
        }

        setSlide(slide);
    }
}
