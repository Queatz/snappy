package com.queatz.snappy.transition;

import android.app.Fragment;
import android.view.View;

/**
 * Created by jacob on 10/25/14.
 */
public class GrandReveal extends Transition {
    public GrandReveal() {
        super();

        mDuration = 800;
    }

    public void onDraw(Fragment frag, float time, Direction direction) {
        if(frag.getView() == null) {
            return;
        }

        float t = (float) Math.pow(direction == Direction.IN ? 1.0f - time : time, 4.0f);
        View view = frag.getView();

        view.setTranslationY(t * ((View) view.getParent()).getHeight());
    }
}
