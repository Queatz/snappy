package com.queatz.snappy.ui;

import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * Created by jacob on 9/26/17.
 */

public class FadePulseAnimation extends Animation {

    private final View view;

    public FadePulseAnimation(View view) {
        this.view = view;
        setInterpolator(new AccelerateDecelerateInterpolator());
        setDuration(1000);
        setRepeatCount(Animation.INFINITE);
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        super.applyTransformation(interpolatedTime, t);

        view.setAlpha(.25f + .75f * Math.abs(interpolatedTime - .5f) * 2f);
        view.postInvalidate();
    }

    @Override
    public boolean willChangeTransformationMatrix() {
        return false;
    }

    @Override
    public boolean willChangeBounds() {
        return false;
    }
}
