package com.queatz.snappy.ui.camera;

import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.queatz.snappy.R;

/**
 * Created by jacob on 8/28/16.
 */

public class AspectView extends RelativeLayout {
    float mAspect;

    public AspectView(android.content.Context context) {
        super(context);
        init(null, 0);
    }

    public AspectView(android.content.Context context, android.util.AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public AspectView(android.content.Context context, android.util.AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        if(attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.SquareLayout, defStyle, 0);

            if (a.hasValue(R.styleable.SquareLayout_aspect)) {
                mAspect = a.getFloat(R.styleable.SquareLayout_aspect, 0);
            } else {
                mAspect = 1f;
            }

            a.recycle();
        }
        else {
            mAspect = 1f;
        }
    }

    public void setAspect(float aspect) {
        mAspect = aspect;
        requestLayout();
    }

    public float getAspect() {
        return mAspect;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        float width = MeasureSpec.getSize(widthMeasureSpec);
        float height = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension((int) (height / mAspect), (int) height);
    }
}
