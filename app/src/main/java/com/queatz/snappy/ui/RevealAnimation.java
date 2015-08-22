package com.queatz.snappy.ui;

import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * Created by jacob on 8/21/15.
 */
public class RevealAnimation {
    public static void expand(final View v) {
        expand(v, -1);
    }

    public static void expand(final View v, final int duration) {
        if(v.getParent() != null) {
            v.measure(
                    View.MeasureSpec.makeMeasureSpec(((ViewGroup) v.getParent()).getWidth(), View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(View.MEASURED_SIZE_MASK, View.MeasureSpec.AT_MOST)
            );
        }
        else {
            v.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        final int targetHeight = v.getMeasuredHeight();

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? ViewGroup.LayoutParams.WRAP_CONTENT
                        : (int)(((float) targetHeight) * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration(duration == -1 ? (int)(targetHeight / v.getContext().getResources().getDisplayMetrics().density) : duration);
        v.startAnimation(a);
    }

    public static void collapse(final View v) {
        collapse(v, -1);
    }

    public static void collapse(final View v, final int duration) {
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if(interpolatedTime == 1) {
                    v.setVisibility(View.GONE);
                }else{
                    v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration(duration == -1 ? (int)(initialHeight / v.getContext().getResources().getDisplayMetrics().density) : duration);
        v.startAnimation(a);
    }
}
