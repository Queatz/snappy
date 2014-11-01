package com.queatz.snappy.ui;

import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.queatz.snappy.R;
import com.queatz.snappy.Util;

/**
 * Created by jacob on 10/31/14.
 */
public class CurrentSlideIndicator extends RelativeLayout {
    private int mCount;
    private float mOffset;
    private FrameLayout mCurrentSlide;
    private LinearLayout mAllSlides;

    public CurrentSlideIndicator(android.content.Context context) {
        super(context);
        init();
    }

    public CurrentSlideIndicator(android.content.Context context, android.util.AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CurrentSlideIndicator(android.content.Context context, android.util.AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void init() {
        mCount = 0;
        mOffset = 0;

        mAllSlides = new LinearLayout(getContext());
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        mAllSlides.setLayoutParams(lp);

        int p = (int) Util.px(getContext(), 16);
        mAllSlides.setPadding(p, p, p, p);
        mAllSlides.setGravity(Gravity.CENTER_HORIZONTAL);

        mCurrentSlide = makeCircle(true);

        addView(mCurrentSlide);
        addView(mAllSlides);
    }

    public void setCount(int count) {
        mCount = count;

        mAllSlides.removeAllViews();

        for(int x = 0; x < mCount; x++)
            mAllSlides.addView(makeCircle(false));

        setOffset(mOffset);
    }

    public void setOffset(float offset) {
        mOffset = offset;

        if(mAllSlides.getChildCount() > 0) {
            float startX = mAllSlides.getChildAt(0).getLeft();
            float endX = mAllSlides.getChildAt(mAllSlides.getChildCount() - 1).getLeft();

            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mCurrentSlide.getLayoutParams();
            lp.topMargin = mAllSlides.getChildAt(0).getTop();
            lp.leftMargin = (int) (startX + (endX - startX) / (float) (mCount - 1) * offset);
            mCurrentSlide.setLayoutParams(lp);
        }
    }

    protected FrameLayout makeCircle(boolean filled) {
        FrameLayout circle = new FrameLayout(getContext());

        if(filled) {
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(MeasureSpec.EXACTLY, MeasureSpec.EXACTLY);

            int m = (int) Util.px(getContext(), 4);
            lp.height = m * 2;
            lp.width = m * 2;
            lp.setMargins(m, m, m, m);
            circle.setLayoutParams(lp);
        }
        else {
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(MeasureSpec.EXACTLY, MeasureSpec.EXACTLY);

            int m = (int) Util.px(getContext(), 4);
            lp.height = m * 2;
            lp.width = m * 2;
            lp.setMargins(m, m, m, m);
            circle.setLayoutParams(lp);
        }

        circle.setBackgroundResource(filled ? R.drawable.circle_full : R.drawable.circle_empty);

        return circle;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        if(changed)
            setOffset(mOffset);
    }
}