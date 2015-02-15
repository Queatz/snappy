package com.queatz.snappy.ui;

import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.queatz.snappy.R;

/**
 * Created by jacob on 10/31/14.
 */
public class SquareLayout extends RelativeLayout {
    float mAspect;

    public SquareLayout(android.content.Context context) {
        super(context);
        init(null, 0);
    }

    public SquareLayout(android.content.Context context, android.util.AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public SquareLayout(android.content.Context context, android.util.AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        if(attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.SquareLayout, defStyle, 0);
            mAspect = a.getFloat(R.styleable.SquareLayout_aspect, 0);
            a.recycle();
        }
        else {
            mAspect = 1f;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int originalWidth = MeasureSpec.getSize(widthMeasureSpec);
        int finalHeight = mAspect == 0 ? originalWidth : (int) (originalWidth / mAspect);

        super.onMeasure(
                MeasureSpec.makeMeasureSpec(originalWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(finalHeight, MeasureSpec.EXACTLY)
        );
    }
}
