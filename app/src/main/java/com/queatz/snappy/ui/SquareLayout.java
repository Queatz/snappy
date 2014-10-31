package com.queatz.snappy.ui;

import android.widget.RelativeLayout;

/**
 * Created by jacob on 10/31/14.
 */
public class SquareLayout extends RelativeLayout {
    public SquareLayout(android.content.Context context) {
        super(context);
    }

    public SquareLayout(android.content.Context context, android.util.AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareLayout(android.content.Context context, android.util.AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        heightMeasureSpec = widthMeasureSpec;
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
