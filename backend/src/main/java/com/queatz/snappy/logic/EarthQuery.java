package com.queatz.snappy.logic;

import com.queatz.snappy.logic.query.EarthQueryFilter;
import com.queatz.snappy.logic.query.EarthQueryLet;
import com.queatz.snappy.shared.Config;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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

    public String aql(String of) {
        String result = (lets.isEmpty() ? "" : lets.stream()
                .map(l -> "let " + l.getVar() + " = (" + l.getValue() + ")\n")
                .collect(Collectors.joining())) +

                (internal ? "" : getVisibleQueryString()) + "for " + x + " in " + in +
                (filters.isEmpty() && internal ? "" : " filter " + (filters.isEmpty() ? "" : (
                        filters.stream()
                                .map(f -> f.getValue() == null ? f.getKey() : (!f.getKey().contains(".") ? x + "." : "") + f.getKey() + " " + f.getComparator() + " " + f.getValue() + "")
                                .collect(Collectors.joining(" and "))
                ) + (internal ? "" : " and ")) + (internal ? "" : getVisibleQueryFilterString())) +
                (sort == null ? "" : " sort " + sort) +
                (limit == null ? "" : " limit " + limit) +
                (of == null ? "" : " return" + (distinct ? " distinct" : "") + " " + of);

        Logger.getLogger(Config.NAME).info(result);

        return result;
    }

    private String getVisibleQueryString() {
        as.requireUser();

        return "let clubs = (\n" +
                "    for club in " + DEFAULT_COLLECTION + "\n" +
                "        for member in " + DEFAULT_COLLECTION + "\n" +
                "            filter club." + EarthField.KIND + " == '" + EarthKind.CLUB_KIND + "'\n" +
                "                and member." + EarthField.KIND + " == '" + EarthKind.MEMBER_KIND + "'\n" +
                "                and member." + EarthField.TARGET + " == club._key\n" +
                "                and member." + EarthField.SOURCE + " == '" + as.getUser().key().name() + "'\n" +
                "                and member." + DEFAULT_FIELD_CONCLUDED + " == null\n" +
                "                and club." + DEFAULT_FIELD_CONCLUDED + " == null\n" +
                "                return distinct club\n" +
                ")" +
                "\n" +
                "let visible = (\n" +
                "    for member in " + in + "\n" +
                "        for club in clubs\n" +
                "            for thing in " + DEFAULT_COLLECTION + "\n" +
                "                filter member." + EarthField.KIND + " == '" + EarthKind.MEMBER_KIND + "'\n" +
                "                    and member." + EarthField.TARGET + " == club._key\n" +
                "                    and member." + EarthField.SOURCE + " == thing._key\n" +
                "                    and member." + DEFAULT_FIELD_CONCLUDED + " == null\n" +
                "                    and thing." + DEFAULT_FIELD_CONCLUDED + " == null\n" +
                "                    and club." + DEFAULT_FIELD_CONCLUDED + " == null\n" +
                "                    return distinct thing\n" +
                ")\n";
    }

    private String getVisibleQueryFilterString() {
        return x + " in visible";
    }
}
