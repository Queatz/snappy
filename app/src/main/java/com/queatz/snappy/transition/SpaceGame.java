package com.queatz.snappy.transition;

import android.app.Fragment;

/**
 * Created by jacob on 10/25/14.
 */
public class SpaceGame extends Transition {
    public SpaceGame() {
        super();
        mDuration = 800;
    }

    public static float get(float t) {
        return (float) Math.pow(t, 16.0f);
    }

    public void onDraw(Fragment frag, float time, Direction direction) {
        if(frag.getView() == null) {
            return;
        }

        float t = get(direction == Direction.IN ? time : 1.0f - time);

        frag.getView().setAlpha(t);
        frag.getView().setScaleX(t);
        frag.getView().setScaleY(t);
    }
}
