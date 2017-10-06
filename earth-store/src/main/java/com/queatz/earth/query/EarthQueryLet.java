package com.queatz.earth.query;

/**
 * Created by jacob on 8/21/17.
 */

public class EarthQueryLet {
    private String var;
    private String value;

    public EarthQueryLet(String var, String value) {
        this.var = var;
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public EarthQueryLet setValue(String value) {
        this.value = value;
        return this;
    }

    public String getVar() {
        return var;
    }

    public EarthQueryLet setVar(String var) {
        this.var = var;
        return this;
    }
}
