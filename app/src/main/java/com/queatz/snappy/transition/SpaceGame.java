package com.queatz.snappy.transition;

import android.app.Fragment;

/**
 * Created by jacob on 10/25/14.
 */
public class SpaceGame extends Transition {
    public SpaceGame() {
        super();
        mDuration = 1200;
    }

    private static float bounce(float t) {
        return t * t * 8.0f;
    }

    public static float get(float t) {
        t *= 1.1226f;
        if (t < 0.3535f) return bounce(t);
        else if (t < 0.7408f) return bounce(t - 0.54719f) + 0.7f;
        else if (t < 0.9644f) return bounce(t - 0.8526f) + 0.9f;
        else return bounce(t - 1.0435f) + 0.95f;
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
