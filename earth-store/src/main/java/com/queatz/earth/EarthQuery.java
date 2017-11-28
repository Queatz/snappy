package com.queatz.earth;

import com.queatz.earth.query.EarthQueryFilter;
import com.queatz.earth.query.EarthQueryLet;
import com.queatz.snappy.as.EarthAs;
import com.queatz.snappy.as.EarthControl;
import com.queatz.snappy.shared.Config;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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
    public String select = null;
    public boolean single = false;
    private String in = EarthStore.DEFAULT_COLLECTION;
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

    public EarthQuery select(String select) {
        this.select = select;
        return this;
    }

    public EarthQuery count(boolean count) {
        this.count = count;
        return this;
    }

    public String aql() {
        return aql(false);
    }

    public String aql(boolean inline) {
        if (as.isInternal()) {
            this.internal = true;
        }

        if (!inline) {
            filter(EarthStore.DEFAULT_FIELD_CONCLUDED, "null");
        }

        if (select == null) {
            select = x;
        }

        String result = (lets.isEmpty() ? "" : lets.stream()
                .map(l -> "let " + l.getVar() + " = (" + l.getValue() + ")\n")
                .collect(Collectors.joining())) +
                (count ? "return count(" : "") +
                (single ? "(" : "") +
                "for " + x + " in " + in +
                (filters.isEmpty() ? "" : " filter " + (
                        filters.stream()
                                .map(f -> f.getValue() == null ? f.getKey() : (!f.getKey().contains(".") ? var() + "." : "") + f.getKey() + " " + f.getComparator() + " " + f.getValue() + "")
                                .collect(Collectors.joining(" and "))
                )) +
                (internal ? "" : getVisibleQueryString()) +
                (sort == null ? "" : " sort " + sort) +
                (limit == null ? "" : " limit " + limit) +
                (inline ? "" : " return" + (distinct ? " distinct" : "") + " " + select) +
                (single ? ")[0]" : "") +
                (count ? ")" : "");

        Logger.getLogger(Config.NAME).info(result);

        return result;
    }

    public String[] vars() {
        return x.split(",");
    }

    public String var() {
        return vars()[0];
    }

    private String getVisibleQueryString() {
        return "\nfilter " + x + "." + EarthField.HIDDEN + " != true" + (as.hasUser() ? " or (\n" +
                "        for t1, r1 in outbound '" + EarthStore.DEFAULT_COLLECTION + "/" + as.getUser().key().name() + "' graph '" + EarthStore.CLUB_GRAPH + "'\n" +
                "            for t2, r2 in outbound " + x + " graph '" + EarthStore.CLUB_GRAPH + "'\n" +
                "                filter r1._to == r2._to limit 1 return true\n" +
                ")[0] == true\n" : "");
    }

    public EarthQuery single() {
        this.single = true;
        return this;
    }
}
