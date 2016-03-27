package com.queatz.snappy.earth.concept;

import com.queatz.snappy.earth.access.As;

/**
 * Created by jacob on 3/26/16.
 */
public class JoinConcept {

    private final As as;

    public JoinConcept(As as) {
        this.as = as;
    }

    public JoinConcept join(Joinable thing) {
        thing.join(as);
        return this;
    }

    public JoinConcept leave(Joinable thing) {
        thing.leave(as);
        return this;
    }
}
