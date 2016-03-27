package com.queatz.snappy.earth.thing;

import com.googlecode.objectify.Ref;

/**
 * Created by jacob on 3/26/16.
 */
public class Thing extends Existence {
    private String name;
    private String description;
    private boolean photo;

    public String getName() {
        return name;
    }

    public Thing setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Thing setDescription(String description) {
        this.description = description;
        return this;
    }

    public boolean isPhoto() {
        return photo;
    }

    public Thing setPhoto(boolean photo) {
        this.photo = photo;
        return this;
    }
}
