package com.queatz.snappy.logic;

/**
 * Created by jacob on 3/12/17.
 */

public class EarthRef {
    private final String name;

    public EarthRef(String name) {
        this.name = name;
    }

    public String name() {
        return null;
    }

    public static EarthRef of(String id) {
        return new EarthRef(id);
    }
}
