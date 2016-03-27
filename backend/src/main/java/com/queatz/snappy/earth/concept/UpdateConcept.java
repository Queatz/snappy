package com.queatz.snappy.earth.concept;

import com.queatz.snappy.earth.access.As;
import com.queatz.snappy.earth.thing.Existence;

/**
 * Created by jacob on 3/26/16.
 */
public class UpdateConcept {

    private final As as;

    public UpdateConcept(As as) {
        this.as = as;
    }

    public UpdateConcept update(Existence thing) {
        return this;
    }
}
