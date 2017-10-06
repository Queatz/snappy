package com.queatz.earth;


import com.queatz.snappy.as.EarthAs;
import com.queatz.snappy.as.EarthControl;

import java.util.HashMap;
import java.util.Map;

/**
 * The class that determines whether or not you can see something.
 */
public class EarthAuthority extends EarthControl {
    public EarthAuthority(final EarthAs as) {
        super(as);
    }

    private static final Map<String, Authority> mapping = new HashMap<>();

    public static void register(String kind, Authority authority) {
        if (mapping.containsKey(kind)) {
            throw new RuntimeException("Cannot replace authority for kind: " + kind + " authority: " + authority);
        }

        mapping.put(kind, authority);
    }

    public boolean authorize(EarthThing entity, EarthRule rule) {
        // Internal access
        if (as == null || as.isInternal()) {
            return true;
        }

        String kind = entity.getString(EarthField.KIND);
        if (mapping.containsKey(kind)) {
            return mapping.get(kind).authorize(as.hasUser() ? as.getUser() : null, entity, rule);
        }

        // If kind has no authority rules, assume access is ok
        return true;
    }
}
