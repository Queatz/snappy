package com.queatz.snappy.earth.concept;

import com.queatz.snappy.earth.access.As;

/**
 * Created by jacob on 3/26/16.
 */
public class FollowConcept extends Concept {

    private FollowConcept(As as) {
        super(as);
    }

    public FollowConcept follow(Followable thing) {
        thing.follow(as);
        return this;
    }

    public FollowConcept unfollow(Followable thing) {
        thing.unfollow(as);
        return this;
    }
}
