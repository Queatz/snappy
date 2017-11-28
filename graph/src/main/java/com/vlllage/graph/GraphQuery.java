package com.vlllage.graph;

import com.queatz.earth.EarthQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by jacob on 11/25/17.
 */

public class GraphQuery {

    private GraphQuery parent;
    private final String field;
    private final EarthQuery earthQuery;

    private final List<GraphQuery> sub = new ArrayList<>();

    private final int varIndex;
    private String tag;

    /**
     * New query
     */
    public GraphQuery() {
        this.field = null;
        this.parent = null;
        this.varIndex = 1;
        this.earthQuery = null;
    }

    /**
     * New field
     */
    public GraphQuery(GraphQuery parent, String field) {
        this.parent = parent;
        this.field = field;
        this.varIndex = -1;
        this.earthQuery = null;
    }

    public GraphQuery(GraphQuery parent) {
        this.field = null;
        this.parent = parent;
        this.varIndex = parent.varIndex + 1;
        this.earthQuery = null;
    }

    public GraphQuery(GraphQuery parent, EarthQuery earthQuery) {
        this.field = null;
        this.parent = parent;
        this.varIndex = parent.varIndex + 1;
        this.earthQuery = earthQuery.as(var());
    }

    public GraphQuery(EarthQuery earthQuery) {
        this.field = null;
        this.parent = null;
        this.varIndex = 1;
        this.earthQuery = earthQuery.as(var());
    }

    public GraphQuery add() {
        GraphQuery query = new GraphQuery(this);
        this.sub.add(query);
        return query;
    }

    public GraphQuery add(String field) {
        GraphQuery query = new GraphQuery(this, field);
        this.sub.add(query);
        return query;
    }

    public GraphQuery add(EarthQuery earthQuery) {
        GraphQuery query = new GraphQuery(this, earthQuery);
        this.sub.add(query);
        return query;
    }

    public String aql() {
        if (field != null) {
            if (parent == null) {
                return field;
            } else {
                return parent.var() + "." + field;
            }
        } else if (!sub.isEmpty()) {
            String str = "[" + sub.stream().map(GraphQuery::aql).collect(Collectors.joining(", ")) + "]";

            if (earthQuery != null) {
                if (parent == null) {
                    return parse(earthQuery.select(str).aql());
                } else {
                    return "(" + parse(earthQuery.select(str).aql()) + ")";
                }
            } else {
                return str;
            }
        } else if (earthQuery != null) {
            return parse(earthQuery.aql());
        } else {
            return "[]";
        }
    }

    private String parse(String aql) {
        return aql
                .replace("{thing}", var())
                .replace("{parent}", parent == null ? "" : parent.var());
    }

    public String var() {
        return "v" + varIndex;
    }

    public GraphQuery setTag(String tag) {
        if (field != null) {
            throw new IllegalStateException("Cannot set tag on field node.");
        }

        this.tag = tag;
        return this;
    }

    public String getTag() {
        return field != null ? field : tag;
    }

    public boolean isList() {
        return field == null;
    }

    public List<GraphQuery> getList() {
        return sub;
    }
}
