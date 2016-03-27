package com.queatz.snappy.earth.thing;

import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.util.Date;

/**
 * Created by jacob on 3/26/16.
 */
public class Existence {
    @Id
    private String id;

    @Index
    private Date created;

    @Index
    private Date deleted;

    public String getId() {
        return id;
    }

    public Existence setId(String id) {
        this.id = id;
        return this;
    }

    public Date getCreated() {
        return created;
    }

    public Existence setCreated(Date created) {
        this.created = created;
        return this;
    }

    public Date getDeleted() {
        return deleted;
    }

    public Existence setDeleted(Date deleted) {
        this.deleted = deleted;
        return this;
    }
}
