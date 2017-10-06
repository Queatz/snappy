package com.queatz.snappy.api;

import org.jetbrains.annotations.NotNull;

/**
 * Created by jacob on 7/16/16.
 */
public class EarthControl {
    protected transient final EarthAs as;

    public EarthControl(@NotNull EarthAs as) {
        this.as = as;
    }

    public <T extends EarthControl> T use(Class<T> clazz) {
        return as.s(clazz);
    }
}
