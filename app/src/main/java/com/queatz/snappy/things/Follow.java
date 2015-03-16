package com.queatz.snappy.things;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.RealmClass;

/**
 * Created by jacob on 2/19/15.
 */

@RealmClass
public class Follow extends RealmObject {
    @Index
    private String id;
    private Person person;
    private Person following;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public Person getFollowing() {
        return following;
    }

    public void setFollowing(Person following) {
        this.following = following;
    }
}
