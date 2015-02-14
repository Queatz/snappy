package com.queatz.snappy.ui;

import android.widget.ImageView;

/**
 * Created by jacob on 10/18/14.
 */
public class SqurePhoto extends ImageView {
    public SqurePhoto(android.content.Context context) {
        super(context);
    }

    public SqurePhoto(android.content.Context context, android.util.AttributeSet attrs) {
        super(context, attrs);
    }

    public SqurePhoto(android.content.Context context, android.util.AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        heightMeasureSpec = widthMeasureSpec;
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
