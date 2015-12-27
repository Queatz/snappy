package com.queatz.snappy.things;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.RealmClass;

/**
 * Created by jacob on 12/26/15.
 */

@RealmClass
public class Endorsement extends RealmObject {
    @Index
    private String id;
    private Person source;
    private Offer target;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Person getSource() {
        return source;
    }

    public void setSource(Person source) {
        this.source = source;
    }

    public Offer getTarget() {
        return target;
    }

    public void setTarget(Offer target) {
        this.target = target;
    }
}
