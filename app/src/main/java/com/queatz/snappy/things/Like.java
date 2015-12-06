package com.queatz.snappy.things;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.RealmClass;

/**
 * Created by jacob on 12/5/15.
 */

// TODO: 12/5/15 - this is very similar to a Follow, which is fairly similar to a Join

@RealmClass
public class Like extends RealmObject {
    @Index
    private String id;
    private Person source;
    private Update target;

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

    public Update getTarget() {
        return target;
    }

    public void setTarget(Update target) {
        this.target = target;
    }
}
