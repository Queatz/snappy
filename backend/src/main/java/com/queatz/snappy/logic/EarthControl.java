package com.queatz.snappy.logic;

import com.google.cloud.datastore.Entity;

/**
 * Created by jacob on 7/16/16.
 */
public class EarthControl {
    protected final EarthAs as;

    public EarthControl(EarthAs as) {
        this.as = as;
    }

    public Entity getUser() {
        return as.getUser();
    }

    public <T extends EarthControl> T use(Class<T> clazz) {
        return as.s(clazz);
    }
}
