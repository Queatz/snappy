package com.queatz.snappy.logic.views;

import com.google.cloud.datastore.Entity;
import com.queatz.snappy.logic.EarthView;

/**
 * Created by jacob on 5/8/16.
 */
public class FollowerView extends LinkView {

    public FollowerView(Entity follower) {
        this(follower, EarthView.DEEP);
    }

    public FollowerView(Entity follower, EarthView view) {
        super(follower, view);
    }
}
