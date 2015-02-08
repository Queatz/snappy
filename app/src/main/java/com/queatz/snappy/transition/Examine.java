package com.queatz.snappy.transition;

import android.app.Fragment;

/**
 * Created by jacob on 11/23/14.
 */
public class Examine extends Transition {
    public Examine() {
        super();

        mDuration = 150;
    }

    public void onDraw(Fragment frag, float time, Direction direction) {
        if(frag.getView() == null) {
            return;
        }

        float t = interpolate(direction == Direction.IN ? time : 1.0f - time);

        frag.getView().setAlpha(t);
        frag.getView().setScaleX(0.95f + 0.05f * t);
        frag.getView().setScaleY(0.95f + 0.05f * t);
    }
}

