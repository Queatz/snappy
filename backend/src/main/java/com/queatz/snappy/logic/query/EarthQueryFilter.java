package com.queatz.snappy.logic.query;

/**
 * Created by jacob on 8/21/17.
 */

public class EarthQueryFilter {
    private String key;
    private String comparator;
    private String value;

    public EarthQueryFilter(String key, String value) {
        this(key, "==", value);
    }

    public EarthQueryFilter(String key, String comparator, String value) {
        this.key = key;
        this.comparator = comparator;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public EarthQueryFilter setKey(String key) {
        this.key = key;
        return this;
    }

    public String getValue() {
        return value;
    }

    public EarthQueryFilter setValue(String value) {
        this.value = value;
        return this;
    }

    public String getComparator() {
        return comparator;
    }

    public EarthQueryFilter setComparator(String comparator) {
        this.comparator = comparator;
        return this;
    }
}
