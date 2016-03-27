package com.queatz.snappy.earth.thing;

import com.googlecode.objectify.Ref;

/**
 * Created by jacob on 3/26/16.
 */
public class Relation extends Existence {
    private Ref<Existence> source;
    private Ref<Existence> target;

    public Ref<Existence> getSource() {
        return source;
    }

    public Relation setSource(Ref<Existence> source) {
        this.source = source;
        return this;
    }

    public Ref<Existence> getTarget() {
        return target;
    }

    public Relation setTarget(Ref<Existence> target) {
        this.target = target;
        return this;
    }
}
