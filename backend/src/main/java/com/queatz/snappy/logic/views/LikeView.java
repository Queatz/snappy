package com.queatz.snappy.logic.views;

import com.google.cloud.datastore.Entity;
import com.queatz.snappy.logic.EarthView;

/**
 * Created by jacob on 5/8/16.
 */
public class LikeView extends LinkView {

    public LikeView(Entity like) {
        this(like, EarthView.DEEP);
    }

    public LikeView(Entity like, EarthView view) {
        super(like, view);
    }
}
