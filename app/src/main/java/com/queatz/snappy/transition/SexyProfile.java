package com.queatz.snappy.transition;

import android.app.Fragment;
import android.view.View;

/**
 * Created by jacob on 10/25/14.
 */
public class SexyProfile extends Transition {
    public void onDraw(Fragment frag, float time, Direction direction) {
        if(frag.getView() == null) {
            return;
        }

        float t = interpolate(direction == Direction.IN ? 1.0f - time : time);
        View view = frag.getView();

        view.setTranslationX(t * ((View) view.getParent()).getWidth());
    }
}
