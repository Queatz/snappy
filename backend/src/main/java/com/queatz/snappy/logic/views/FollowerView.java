package com.queatz.snappy.logic.views;

import com.google.cloud.datastore.Entity;
import com.queatz.snappy.logic.EarthAs;
import com.queatz.snappy.logic.EarthView;

/**
 * Created by jacob on 5/8/16.
 */
public class FollowerView extends LinkView {

    public FollowerView(EarthAs as, Entity follower) {
        this(as, follower, EarthView.DEEP);
    }

    public FollowerView(EarthAs as, Entity follower, EarthView view) {
        super(as, follower, view);
    }
}
