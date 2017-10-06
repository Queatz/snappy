package com.queatz.snappy.logic.views;

import com.queatz.snappy.logic.EarthAs;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.logic.EarthView;

/**
 * Created by jacob on 5/8/16.
 */
public class LikeView extends LinkView {

    public LikeView(EarthAs as, EarthThing like) {
        this(as, like, EarthView.DEEP);
    }

    public LikeView(EarthAs as, EarthThing like, EarthView view) {
        super(as, like, view);
    }
}
