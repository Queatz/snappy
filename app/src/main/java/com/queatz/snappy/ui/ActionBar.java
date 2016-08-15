package com.queatz.snappy.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.queatz.snappy.R;
import com.queatz.snappy.Util;

/**
 * Created by jacob on 10/19/14.
 */

// Controlled by SlideScreen

public class ActionBar extends FrameLayout {
    public interface OnPageChangeListener {
        void onPageChange(int page);
    }

    public abstract static class TabAdapter {
        public abstract int getCount();
        public abstract String getTabName(int tab);
    }

    private ViewGroup mTabBar;

    private TextView mTitle;
    private ImageView mImg;
    private View mUpButton;
    private FrameLayout mLeftContent;
    private FrameLayout mRightContent;
    private View mUnderline;
    private View mSlider;

    private float mSlidePosition;

    private OnPageChangeListener mOnPageChangeListener;
    private TabAdapter mTabAdapter;

    public ActionBar(Context context) {
        super(context);
        init();
    }

    public ActionBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ActionBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.actionbar, this, true);

        mTitle = (TextView) findViewById(R.id.title);
        mImg = (ImageView) findViewById(R.id.img);
        mTabBar = (ViewGroup) findViewById(R.id.tabbar);
        mUpButton = findViewById(R.id.upButton);
        mLeftContent = (FrameLayout) findViewById(R.id.leftContent);
        mRightContent = (FrameLayout) findViewById(R.id.rightContent);
        mUnderline = findViewById(R.id.underline);
        mSlider = findViewById(R.id.slider);

        if(Build.VERSION.SDK_INT >= 21) {
            setBackgroundResource(R.color.white);
            setElevation(Util.px(2));
        }

        makeTabs();
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean b, int i, int i1, int i2, int i3) {
        super.onLayout(b, i, i1, i2, i3);

        if(b)
            setSlide(mSlidePosition);
    }

    public void setTitle(String title) {
        mTitle.setText(title);
        mImg.setVisibility(View.GONE);
    }

    public void showImg() {
        mTitle.setVisibility(View.GONE);
        mImg.setVisibility(View.VISIBLE);
    }

    public FrameLayout getLeftContent() {
        return mLeftContent;
    }

    public FrameLayout getRightContent() {
        return mRightContent;
    }

    public void setLeftContent(View.OnClickListener action) {
        mUpButton.setVisibility(View.VISIBLE);
        mUpButton.setOnClickListener(action);
    }

    public void setLeftContent(String action) {
        mUpButton.setVisibility(View.VISIBLE);
        mUpButton.setTag(action);
        ((Activity) getContext()).registerForContextMenu(mRightContent);
    }

    public void setRightContent(View.OnClickListener action) {
        mRightContent.setVisibility(View.VISIBLE);
        mRightContent.setOnClickListener(action);
    }

    public void setRightContent(String action) {
        mRightContent.setVisibility(View.VISIBLE);
        mRightContent.setTag(action);
        ((Activity) getContext()).registerForContextMenu(mRightContent);
    }

    public void setAdapter(TabAdapter tabAdapter) {
        mTabAdapter = tabAdapter;
        makeTabs();
    }

    private void makeTabs() {
        mTabBar.removeAllViews();

        if(mTabAdapter == null)
            return;

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        for(int x = 0; x < mTabAdapter.getCount(); x++) {
            TextView tab = (TextView) inflater.inflate(R.layout.actionbartab, null, false);
            tab.setText(mTabAdapter.getTabName(x));
            mTabBar.addView(tab);

            final int _x = x;
            tab.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    setPage(_x);
                }
            });
        }
    }

    public void setOnPageChangeListener(OnPageChangeListener a) {
        mOnPageChangeListener = a;
    }

    public void selectPage(int page) {
        page = Math.max(0, Math.min(mTabAdapter.getCount() - 1, page));
        for(int x = 0; x < mTabBar.getChildCount(); x++) {
            ((TextView) mTabBar.getChildAt(x)).setTextColor(getResources().getColor(page == x ? R.color.white : R.color.whiteout));
        }
    }

    public void setPage(int page) {
        selectPage(page);

        if(mOnPageChangeListener != null) {
            mOnPageChangeListener.onPageChange(page);
        }
    }

    public void setSlide(float f) {
        if(mTabBar.getChildCount() < 1)
            return;

        mSlidePosition = f;

        int t1 = (int) Math.floor(mSlidePosition);
        float offset = mSlidePosition - (float) t1;

        if(t1 >= mTabBar.getChildCount()) {
            t1 = mTabBar.getChildCount() - 1;
            offset = 0.0f;
        }

        if(t1 < 0) {
            t1 = 0;
            offset = 0.0f;
        }

        View v1 = mTabBar.getChildAt(t1);
        View v2 = mTabBar.getChildAt(t1 == mTabBar.getChildCount() - 1 ? t1 : t1 + 1);

        if(v1 == null || v2 == null)
            return;

        float x1 = v1.getLeft() + v1.getPaddingLeft();
        float x2 = v2.getLeft() + v2.getPaddingLeft();
        float w1 = v1.getWidth() - v1.getPaddingLeft() - v1.getPaddingRight();
        float w2 = v2.getWidth() - v2.getPaddingLeft() - v2.getPaddingRight();

        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mSlider.getLayoutParams();
        lp.width = (int) (w1 * (1.0f - offset) + w2 * offset);
        lp.leftMargin = ((View) mTabBar.getParent()).getLeft() + (int) (x1 * (1.0f - offset) + x2 * offset);
        mSlider.setLayoutParams(lp);
    }

    public void resolve() {
        setPage((int) mSlidePosition);
    }
}
