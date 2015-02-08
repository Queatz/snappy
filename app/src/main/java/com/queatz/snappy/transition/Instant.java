package com.queatz.snappy.transition;

import android.app.Fragment;

/**
 * Created by jacob on 10/25/14.
 */
public class Instant extends Transition {
    public Instant() {
        super();

        mDuration = 0;
    }

    public void onDraw(Fragment frag, float time, Direction direction) {
    }
}
