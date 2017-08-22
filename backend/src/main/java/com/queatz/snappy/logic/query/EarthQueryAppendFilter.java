package com.queatz.snappy.logic.query;

/**
 * Created by jacob on 8/21/17.
 */

public class EarthQueryAppendFilter {

    private String first;
    private String second;

    public EarthQueryAppendFilter(String first, String second) {
        this.first = first;
        this.second = second;
    }

    public String getFirst() {
        return first;
    }

    public EarthQueryAppendFilter setFirst(String first) {
        this.first = first;
        return this;
    }

    public String getSecond() {
        return second;
    }

    public EarthQueryAppendFilter setSecond(String second) {
        this.second = second;
        return this;
    }

    public String aql() {
        return "append(" + first + ", (" + second + "))";
    }
}
