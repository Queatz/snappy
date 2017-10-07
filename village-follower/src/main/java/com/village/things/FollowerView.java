package com.village.things;

import com.queatz.earth.EarthThing;
import com.queatz.snappy.as.EarthAs;
import com.queatz.snappy.view.EarthView;

/**
 * Created by jacob on 5/8/16.
 */
public class FollowerView extends LinkView {

    public FollowerView(EarthAs as, EarthThing follower) {
        this(as, follower, EarthView.DEEP);
    }

    public FollowerView(EarthAs as, EarthThing follower, EarthView view) {
        super(as, follower, view);
    }
}
