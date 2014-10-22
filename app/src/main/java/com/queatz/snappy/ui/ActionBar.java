package com.queatz.snappy.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.queatz.snappy.R;

/**
 * Created by jacob on 10/19/14.
 */

// Controlled by SlideScreen

public class ActionBar extends FrameLayout {
    public interface OnPageChangeListener {
        public void onPageChange(int page);
    }

    public interface TabAdapter {
        public int getCount();
        public String getTabName(int tab);
    }

    private ViewGroup mTabBar;

    private TextView mTitle;
    private View mUpButton;
    private View mLeftContent;
    private View mRightContent;
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
        mTabBar = (ViewGroup) findViewById(R.id.tabbar);
        mUpButton = findViewById(R.id.upButton);
        mLeftContent = findViewById(R.id.leftContent);
        mRightContent = findViewById(R.id.rightContent);
        mUnderline = findViewById(R.id.underline);
        mSlider = findViewById(R.id.slider);

        makeTabs();
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean b, int i, int i1, int i2, int i3) {
        super.onLayout(b, i, i1, i2, i3);

        setSlide(mSlidePosition);
    }

    public void setTitle(String title) {
        mTitle.setText(title);
    }

    public void setLeftContent(View.OnClickListener action) {
        mUpButton.setVisibility(View.VISIBLE);
        mUpButton.setOnClickListener(action);
    }

    public void setRightContent(View.OnClickListener action) {
        mRightContent.setVisibility(View.VISIBLE);
        mRightContent.setOnClickListener(action);
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
                    setPage(_x); // XX just for fun for now, really is handled by SlideScreen

                    if(mOnPageChangeListener != null) {
                        mOnPageChangeListener.onPageChange(_x);
                    }
                }
            });
        }
    }

    public void setOnPageChangeListener(OnPageChangeListener a) {
        mOnPageChangeListener = a;
    }

    public void setPage(int page) {
        setSlide(page);

        for(int x = 0; x < mTabBar.getChildCount(); x++) {
            ((TextView) mTabBar.getChildAt(x)).setTextColor(getResources().getColor(page == x ? R.color.red : R.color.info));
        }

        if(mOnPageChangeListener != null) {
            mOnPageChangeListener.onPageChange(page);
        }
    }

    public void setSlide(float f) {
        if(mTabBar.getChildCount() < 1)
            return;

        mSlidePosition = f;

        int t1 = (int) Math.floor(mSlidePosition);
        float offset = mSlidePosition - t1;

        if(t1 >= mTabBar.getChildCount()) {
            t1 = mTabBar.getChildCount() - 1;
            offset = 0f;
        }

        View v1 = mTabBar.getChildAt(t1);
        View v2 = mTabBar.getChildAt(t1 == mTabBar.getChildCount() - 1 ? t1 : t1 + 1);
        float x1 = v1.getLeft() + v1.getPaddingLeft();
        float x2 = v2.getLeft() + v2.getPaddingLeft();
        float w1 = v1.getWidth() - v1.getPaddingLeft() - v1.getPaddingRight();
        float w2 = v2.getWidth() - v2.getPaddingLeft() - v2.getPaddingRight();

        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mSlider.getLayoutParams();
        lp.width = (int) (w1 * (1f - offset) + w2 * offset);
        lp.leftMargin = ((View) mTabBar.getParent()).getLeft() + (int) (x1 + x2 * offset);
        mSlider.setLayoutParams(lp);
    }
}
