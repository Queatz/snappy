package com.queatz.snappy.logic;

import com.queatz.earth.EarthField;
import com.queatz.snappy.logic.query.EarthQueryFilter;
import com.queatz.snappy.logic.query.EarthQueryLet;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.queatz.snappy.logic.EarthStore.CLUB_GRAPH;
import static com.queatz.snappy.logic.EarthStore.DEFAULT_COLLECTION;
import static com.queatz.snappy.logic.EarthStore.DEFAULT_FIELD_CONCLUDED;

/**
 * Created by jacob on 8/21/17.
 */

public class EarthQuery extends EarthControl {

    private List<EarthQueryFilter> filters;
    private List<EarthQueryLet> lets;
    private String sort = null;
    private String limit = null;
    private boolean internal = false;
    private boolean distinct = false;
    private String x = "x";
    private String in = DEFAULT_COLLECTION;
    private boolean count = false;

    public EarthQuery(@NotNull EarthAs as) {
        super(as);

        filters = new ArrayList<>();
        lets = new ArrayList<>();
    }

    public EarthQuery filter(String key, String comparator, String value) {
        filters.add(new EarthQueryFilter(key, comparator, value));
        return this;
    }

    public EarthQuery filter(String key, String value) {
        filters.add(new EarthQueryFilter(key, value));
        return this;
    }

    public EarthQuery filter(String filter) {
        filters.add(new EarthQueryFilter(filter, null));
        return this;
    }

    public EarthQuery sort(String sort) {
        this.sort = sort;
        return this;
    }

    public EarthQuery limit(String limit) {
        this.limit = limit;
        return this;
    }

    public EarthQuery internal(boolean internal) {
        this.internal = internal;
        return this;
    }

    public EarthQuery distinct(boolean distinct) {
        this.distinct = distinct;
        return this;
    }

    public EarthQuery in(String in) {
        this.in = in;
        return this;
    }

    public EarthQuery let(String var, String value) {
        lets.add(new EarthQueryLet(var, value));
        return this;
    }

    public EarthQuery as(String x) {
        this.x = x;
        return this;
    }

    public String aql() {
        return aql(x);
    }

    public String aql(@Nullable String of) {
        if (as.isInternal()) {
            this.internal = true;
        }

        if (of != null) {
            filter(DEFAULT_FIELD_CONCLUDED, "null");
        }

        String result = (lets.isEmpty() ? "" : lets.stream()
                .map(l -> "let " + l.getVar() + " = (" + l.getValue() + ")\n")
                .collect(Collectors.joining())) +
                (count ? "return count(" : "") +
                "for " + x + " in " + in +
                (filters.isEmpty() ? "" : " filter " + (
                        filters.stream()
                                .map(f -> f.getValue() == null ? f.getKey() : (!f.getKey().contains(".") ? gX() + "." : "") + f.getKey() + " " + f.getComparator() + " " + f.getValue() + "")
                                .collect(Collectors.joining(" and "))
                )) + (internal ? "" : getVisibleQueryString()) +
                (sort == null ? "" : " sort " + sort) +
                (limit == null ? "" : " limit " + limit) +
                (of == null ? "" : " return" + (distinct ? " distinct" : "") + " " + of) +
                (count ? ")" : "");

//        Logger.getLogger(Config.NAME).info(result);

        return result;
    }

    private String gX() {
        return x.split(",")[0];
    }

    private String getVisibleQueryString() {
        return "\nfilter " + x + "." + EarthField.HIDDEN + " != true" + (as.hasUser() ? " or (\n" +
                "        for t1, r1 in outbound '" + DEFAULT_COLLECTION + "/" + as.getUser().key().name() + "' graph '" + CLUB_GRAPH + "'\n" +
                "            for t2, r2 in outbound " + x + " graph '" + CLUB_GRAPH + "'\n" +
                "                filter r1._to == r2._to limit 1 return true\n" +
                ")[0] == true\n" : "");
    }

    public EarthQuery count(boolean count) {
        this.count = count;
        return this;
    }
}
