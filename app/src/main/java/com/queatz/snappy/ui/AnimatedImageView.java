package com.queatz.snappy.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by jacob on 9/25/15.
 */
public class AnimatedImageView extends ImageView {
    private Matrix matrix;
    private long start;
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            loop();
        }
    };

    public AnimatedImageView(Context context) {
        super(context);
        init();
    }

    public AnimatedImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AnimatedImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        matrix = new Matrix();

        setScrollContainer(true);
        setScaleType(ImageView.ScaleType.MATRIX);
        setImageMatrix(matrix);
    }

    private float delta() {
        return ((System.currentTimeMillis() - start) / 80000f) % 1f;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        postDelayed(new Runnable() {
            @Override
            public void run() {
                start = System.currentTimeMillis();
                removeCallbacks(runnable);
                post(runnable);
            }
        }, 3000);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks(runnable);
    }

    private void loop() {
        invalidate();

        postDelayed(runnable, 50);
    }

    @Override
    public void onDraw(@NonNull Canvas canvas) {
        float delta = (float) (Math.sin(delta() * Math.PI * 2f - Math.PI / 2f) + 1f) / 2f;

        matrix.reset();

        float viewW = getMeasuredWidth();
        float viewH = getMeasuredHeight();
        float imageW = getDrawable().getIntrinsicWidth();
        float imageH = getDrawable().getIntrinsicHeight();

        float scale = viewH / imageH;

        matrix.postScale(scale, scale);
        matrix.postTranslate(delta * (viewW - imageW * scale), 0);
        setImageMatrix(matrix);

        super.onDraw(canvas);
    }
}
