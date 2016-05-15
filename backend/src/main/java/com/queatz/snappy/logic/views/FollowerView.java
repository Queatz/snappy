package com.queatz.snappy.logic.views;

import com.google.cloud.datastore.Entity;

/**
 * Created by jacob on 5/8/16.
 */
public class FollowerView extends LinkView {
    public FollowerView(Entity follower) {
        super(follower);
    }
}
