package com.queatz.snappy.transition;

import android.app.Fragment;

/**
 * Created by jacob on 10/25/14.
 */
public class InTheVoid extends Transition {
    public void onDraw(Fragment frag, float time, Direction direction) {
        if(frag.getView() == null) {
            return;
        }

        float t = interpolate(direction == Direction.IN ? time : 1.0f - time);

        frag.getView().setAlpha(t);
        frag.getView().setScaleX(0.75f + 0.25f * t);
        frag.getView().setScaleY(0.75f + 0.25f * t);
    }
}