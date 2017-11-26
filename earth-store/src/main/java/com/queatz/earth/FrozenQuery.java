package com.queatz.earth;

import java.util.Map;

/**
 * Created by jacob on 11/27/17.
 */

public class FrozenQuery {

    private final EarthQuery earthQuery;
    private final Map<String, Object> vars;

    public FrozenQuery(EarthQuery earthQuery, Map<String, Object> vars) {
        this.earthQuery = earthQuery;
        this.vars = vars;
    }

    public EarthQuery getEarthQuery() {
        return earthQuery;
    }

    public Map<String, Object> getVars() {
        return vars;
    }
}
