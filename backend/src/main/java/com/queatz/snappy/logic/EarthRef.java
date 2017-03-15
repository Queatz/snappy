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
        return this.name;
    }

    public static EarthRef of(String name) {
        return new EarthRef(name);
    }

    @Override
    public boolean equals(Object o) {
        return name != null &&
                o != null &&
                EarthRef.class.isAssignableFrom(o.getClass()) &&
                name.equals(((EarthRef) o).name);
    }
}
